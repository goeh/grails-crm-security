/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.security

import grails.plugins.crm.core.CrmException
import grails.plugins.crm.core.DateUtils
import grails.plugins.crm.core.Pair
import grails.plugins.crm.feature.Feature
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod

import java.util.regex.Pattern

/**
 * Account Management.
 */
class CrmAccountService {

    private static final Pattern QUANTITY_UNIT_PATTERN = ~/^(\d+)\s*([a-zA-Z]?\w*)$/

    def grailsApplication
    def crmSecurityService
    def crmFeatureService

    /**
     * Create a new account.
     * @param params
     * @param map with products to add to the account
     * @return the created CrmAccount instance
     */
    CrmAccount createAccount(Map<String, Object> params = [:], Object products = null) {
        def user = params.user
        if (!user) {
            user = crmSecurityService.getCurrentUser()
            if (!user) {
                throw new IllegalArgumentException("Can't create tenant because user is not authenticated")
            }
            if (!user.enabled) {
                throw new CrmException("user.not.found.message", [user.username])
            }
        }
        def account = new CrmAccount()
        def args = [account, params, [include: CrmAccount.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(account, 'bind', args.toArray())

        account.user = user

        def status = params.status
        if (status) {
            if (status instanceof Number) {
                account.status = status
            } else {
                account.setStatusText(status.toString())
            }
        }

        if (!account.name) {
            account.name = user.name
        }
        if (!account.email) {
            account.email = user.email
        }

        def expires = params.remove('expires')
        if (expires) {
            if (expires instanceof Date) {
                if (!(expires instanceof java.sql.Date)) {
                    expires = new java.sql.Date(expires.clearTime().time)
                }
            } else {
                expires = DateUtils.parseSqlDate(expires.toString())
            }
            account.expires = expires
        }

        params.options.each { key, value ->
            account.setOption(key, value)
        }

        if (products) {
            if (products instanceof Map) {
                products.each { p, n ->
                    def m = QUANTITY_UNIT_PATTERN.matcher(n.toString())
                    def unit
                    if (m.matches()) {
                        n = Integer.valueOf(m.group(1))
                        unit = m.group(2)
                    }
                    if (!unit) {
                        unit = 'st'
                    }
                    account.addToItems(productId: p, quantity: n, unit: unit)
                }
            } else if (products instanceof Collection) {
                for (p in products) {
                    account.addToItems(productId: p, quantity: 1, unit: 'st')
                }
            }
        }

        account.save(failOnError: true, flush: true)

        event(for: "crm", topic: "accountCreated", data: account.dao)

        return account
    }

    List<CrmAccount> getAccounts(String username = null) {
        if (!username) {
            username = crmSecurityService.getCurrentUser()?.username
            if (!username) {
                throw new IllegalArgumentException("Can't list accounts because user is not authenticated")
            }
        }
        CrmAccount.createCriteria().list([sort: 'name', order: 'asc']) {
            user {
                eq('username', username)
            }
            cache true
        }
    }

    CrmAccount getCurrentAccount() {
        def crmUser = crmSecurityService.getCurrentUser()
        if (!crmUser) {
            throw new IllegalArgumentException("User is not authenticated")
        }
        def tenant = crmSecurityService.getCurrentTenant()
        if (tenant) {
            def account = tenant.account
            if (account.user.id == crmUser.id) {
                return account
            }
        }
        return null
    }

    void closeAccount(Long id) {
        def crmAccount = CrmAccount.get(id)
        if (!crmAccount) {
            throw new CrmException('crmAccount.not.found.message', ['Account', id])
        }
        // Close account
        def yesterday = new java.sql.Date((new Date().clearTime() - 1).time)
        crmAccount.status = CrmAccount.STATUS_CLOSED
        crmAccount.expires = yesterday
        crmAccount.save(failOnError: true)

        def tenants = crmAccount.tenants*.id
        if (tenants) {
            def roles = CrmUserRole.createCriteria().list() {
                role {
                    inList('tenantId', tenants)
                }
            }
            roles*.delete()
            def permissions = CrmUserPermission.createCriteria().list() {
                inList('tenantId', tenants)
            }
            permissions*.delete()
        }
        // Use platform-core events to broadcast that the account was closed.
        // Receivers could remove or reset any data associated with the account.
        event(for: "crm", topic: "accountClosed", data: crmAccount.dao)
    }

    void deleteAccount(CrmAccount crmAccount) {
        if (crmAccount.tenants) {
            throw new CrmException('crmAccount.not.empty.message', ['Account', crmAccount.id])
        }

        def accountInfo = crmAccount.dao

        crmAccount.delete(flush: true)

        // Use platform-core events to broadcast that the account was deleted.
        // Receivers should remove any data associated with the account.
        event(for: "crm", topic: "accountDeleted", data: accountInfo)
    }

    boolean transferTenant(Long tenantId, CrmAccount toAccount = null) {
        def crmTenant = CrmTenant.get(tenantId)
        if (!crmTenant) {
            throw new CrmException('tenant.not.found.message', ['Tenant', tenantId])
        }
        def rval
        if (toAccount) {
            crmTenant.account = toAccount
            crmTenant.transfer = null
            crmTenant.save(flush: true)

            def crmUser = toAccount.user
            crmSecurityService.addSystemRole(crmUser, 'admin', crmTenant.id)

            toAccount.refresh()

            toAccount.setItem('crmTenant', toAccount.tenants.size())

            def stats = getRoleStatistics(toAccount)
            def maxAdmins = toAccount.getItem('crmAdmin')?.quantity ?: 0
            def maxUsers = toAccount.getItem('crmUser')?.quantity ?: 0
            def maxGuests = toAccount.getItem('crmGuest')?.quantity ?: 0
            def currentAdmins = stats.admin?.size() ?: 0
            def currentUsers = stats.user?.size() ?: 0
            def currentGuests = stats.guest?.size() ?: 0
            if (currentAdmins > maxAdmins) {
                toAccount.setItem('crmAdmin', currentAdmins)
            }
            if (currentUsers > maxUsers) {
                toAccount.setItem('crmUser', currentUsers)
            }
            if (currentGuests > maxGuests) {
                toAccount.setItem('crmGuest', currentGuests)
            }

            toAccount.save()

            event(for: "crmTenant", topic: "transferred", data: crmTenant.dao)
            rval = true
        } else {
            crmTenant.transfer = new Date()
            crmTenant.save(flush: true)
            event(for: "crmTenant", topic: "transfer", data: crmTenant.dao)
            rval = false
        }
        return rval
    }


    Map getRoleStatistics(CrmAccount crmAccount) {
        def statistics = [:]
        for (tenant in crmAccount.tenants*.ident()) {
            def result = CrmUserRole.createCriteria().list() {
                role {
                    eq('tenantId', tenant)
                }
                cache true
            }
            for (userrole in result) {
                statistics.get(userrole.role.name, [] as Set) << userrole.user.username
            }
        }
        return statistics
    }

    List<Feature> getAccountFeatures(CrmAccount crmAccount) {
        def result = []
        for (p in crmAccount.items*.productId) {
            def f = crmFeatureService.getApplicationFeature(p)
            if (f) {
                result << f
            }
        }
        return result
    }

    boolean hasItem(CrmAccount account, String productId, Integer quantity = null) {
        def item = account.items?.find { it.productId == productId }
        if (item) {
            return quantity ? item.quantity >= quantity : true
        }
        return false
    }

    CrmAccountItem getItem(CrmAccount account, String productId) {
        account.items?.find { it.productId == productId }
    }

    CrmAccountItem updateItem(CrmAccount account, String productId, Integer quantity, String unit = null) {
        def item = account.items?.find { it.productId == productId }
        if (item) {
            if (!unit) {
                unit = 'st'
            }
            item.quantity = quantity
            item.unit = unit
        }
        return item
    }

    CrmAccountItem addItem(CrmAccount account, String productId, Integer quantity = null, String unit = null) {
        if (quantity == null) {
            quantity = 1
        }
        if (!unit) {
            unit = 'st'
        }
        def item = new CrmAccountItem(account: account, productId: productId, quantity: quantity, unit: unit)
        if (item.validate()) {
            account.addToItems(item)
        }
        return item
    }

    Integer getQuantity(CrmAccount account, String productId) {
        getQuantityAndUnit(account, productId)?.getLeft() ?: 0
    }

    String getUnit(CrmAccount account, String productId) {
        getQuantityAndUnit(account, productId)?.getRight() ?: 'st'
    }

    Pair<Integer, String> getQuantityAndUnit(CrmAccount account, String productId) {
        def result = CrmAccountItem.createCriteria().get() {
            projections {
                property('quantity')
                property('unit')
            }
            eq('account', account)
            eq('productId', productId)
            cache true
        }
        return result ? new Pair<Integer, String>(* result) : null
    }

    CrmTenant createTrialAccount(CrmUser user, Map params, Locale locale = null) {
        if (!locale) {
            locale = Locale.getDefault()
        }
        def cal = Calendar.getInstance(locale)
        cal.setTime(new Date())
        def trialDays = grailsApplication.config.crm.account.trialDays ?: 30
        cal.add(Calendar.DAY_OF_MONTH, trialDays)
        // Be nice and expire on Monday 00:00:00
        // TODO when displayed using formatDate it says "expires on Monday" and
        // the user may think he/she can use the application all day on Monday
        // but that's not the case. And we can't store it as Sunday 23:59:59
        // as it is stored as java.sql.Date without hour/minute components.
        if (params == null) {
            params = [:]
        }
        params.user = user
        params.expires = DateUtils.startOfWeek(1, DateUtils.endOfWeek(0, cal.time))
        params.status = CrmAccount.STATUS_TRIAL
        def account = createAccount(params, grailsApplication.config.crm.account.autoCreate ?: [:])
        def tenant = crmSecurityService.createTenant(account, user.name, [locale: locale])
        for (item in account.items) {
            def a = crmFeatureService.getApplicationFeature(item.productId)
            if (a) {
                crmFeatureService.enableFeature(a.name, tenant.id)
            }
        }
        return tenant
    }
}
