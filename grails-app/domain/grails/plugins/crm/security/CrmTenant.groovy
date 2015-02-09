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

import grails.plugins.crm.core.AuditEntity
import grails.plugins.crm.core.TenantUtils

/**
 * This domain class represents a tenant, also known as "view".
 * A user can be given access to multiple tenants but only have one tenant active at a given time.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
@AuditEntity
class CrmTenant {

    // Long id of this account will be used as tenantId for all instances created by this tenant.
    String locale
    String currency
    String name
    Date transfer
    CrmTenant parent

    static belongsTo = [account: CrmAccount]
    static hasMany = [options: CrmTenantOption]
    static constraints = {
        locale(maxSize: 5, nullable: true, blank: false)
        currency(maxSize: 4, nullable: true)
        name(size: 2..80, maxSize: 80, nullable: false, blank: false, unique: 'account')
        transfer(nullable: true)
        parent(nullable: true)
    }
    static mapping = {
        table 'crm_tenant'
        sort 'name'
        cache 'nonstrict-read-write'
    }

    static transients = ['dao', 'children', 'current', 'user', 'localeInstance']

    /**
     * Returns the name property.
     * @return name property
     */
    String toString() {
        name
    }

    transient boolean isCurrent() {
        id == TenantUtils.getTenant()
    }

    transient CrmUser getUser() {
        account.user
    }

    transient List<CrmTenant> getChildren() {
        CrmTenant.findAllByParent(this)
    }

    transient Locale getLocaleInstance() {
        locale ? new Locale(* locale.split('_')) : Locale.getDefault()
    }

    /**
     * Clients should use this method to get tenant properties instead of accessing the domain instance directly.
     * The following properties are returned as a Map: [Long id, String name, Map account [id, name, email]]
     * @return a data access object (Map) representing the domain instance.
     */
    transient Map<String, Object> getDao() {
        [id: id, name: name, parent: parent?.id, locale: getLocaleInstance(),
                account: [id: account.id, name: account.name, email: account.email, telephone: account.telephone,
                        user: [username: account.user?.username, email: account.user?.email, name: account.user?.name],
                active: account.isActive(), theme: account.getOption(CrmThemeService.OPTION_THEME_NAME)],
                options: getOptionsMap(), dateCreated: dateCreated]
    }

    /**
     * Return tenant parameters (options) as a Map.
     *
     * @return options
     */
    private Map<String, Object> getOptionsMap() {
        options.inject([:]) { map, o ->
            map[o.key] = o.value
            map
        }
    }

    void setOption(String key, Object value) {
        if (value == null) {
            removeOption(key)
        } else {
            def o = options.find { it.key == key }
            if (o != null) {
                o.value = value
            } else {
                addToOptions(new CrmTenantOption(key, value))
            }
        }
    }

    def getOption(String key = null) {
        if (key == null) {
            return getOptionsMap()
        }
        def o = options.find { it.key == key }
        return o != null ? o.value : null
    }

    boolean hasOption(String key) {
        getOption(key)
    }

    boolean removeOption(String key) {
        def o = options.find { it.key == key }
        if (o != null) {
            removeFromOptions(o)
            o.delete()
            return true
        }
        return false
    }
}
