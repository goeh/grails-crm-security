/*
 * Copyright (c) 2014 Goran Ehrsson.
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

/**
 * CrmAccount related tags.
 */
class CrmAccountTagLib {

    static namespace = "crm"

    def crmAccountService

    def accountQuotaGreaterThan = { attrs, body ->
        def account = attrs.account
        if (!account) {
            account = crmAccountService.getCurrentAccount()
        }
        if (account) {
            def quota = attrs.quota
            if (!quota) {
                throwTagError("Tag [accountQuotaGreaterThan] is missing required attribute [quota]")
            }
            def reference = Integer.valueOf(attrs.value ?: 0)
            def value = account?.getOption(quota) ?: 0
            if (value > reference) {
                out << body()
            }
        }
    }

    def isAllowedMoreTenants = { attrs, body ->
        def account = attrs.account
        if (!account) {
            account = crmAccountService.getCurrentAccount()
        }
        if (account) {
            def max = account.getItem('crmTenant')?.quantity ?: 1
            def size = account.tenants?.size() ?: 0
            if (size < max) {
                out << body()
            }
        }
    }

    def isAllowedMoreInvitations = { attrs, body ->
        def account = attrs.account
        if (!account) {
            account = crmAccountService.getCurrentAccount()
        }
        if (account) {
            def stats = crmAccountService.getRoleStatistics(account)
            def maxAdmins = account.getItem('crmAdmin')?.quantity ?: 1
            def maxUsers = account.getItem('crmUser')?.quantity ?: 0
            def maxPartners = account.getItem('crmPartner')?.quantity ?: 0
            def maxGuests = account.getItem('crmGuest')?.quantity ?: 0
            def currentAdmins = stats.admin?.size() ?: 0
            def currentUsers = stats.user?.size() ?: 0
            def currentPartners = stats.partner?.size() ?: 0
            def currentGuests = stats.guest?.size() ?: 0
            def moreAdmins = maxAdmins - currentAdmins
            def moreUsers = maxUsers - currentUsers
            def morePartners = maxPartners - currentPartners
            def moreGuests = maxGuests - currentGuests
            def ok
            switch (attrs.role) {
                case "admin":
                    ok = moreAdmins > 0
                    break
                case "user":
                    ok = moreUsers > 0
                    break
                case "partner":
                    ok = morePartners > 0
                    break
                case "guest":
                    ok = moreGuests > 0
                    break
                default:
                    ok = (moreAdmins + moreUsers + morePartners + moreGuests) > 0
                    break
            }
            if (ok) {
                out << body()
            }
        }
    }

}
