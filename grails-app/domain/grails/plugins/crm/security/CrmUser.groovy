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
import grails.plugins.crm.core.UuidEntity

/**
 * This domain class represents a user account.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
@AuditEntity
@UuidEntity
class CrmUser {

    String username
    String email
    String name
    String company
    String telephone
    String timezone
    String postalCode
    String countryCode
    String campaign
    Integer status = STATUS_NEW
    Integer loginFailures = 0
    Long defaultTenant


    public static final Integer STATUS_CLOSED = -2
    public static final Integer STATUS_BLOCKED = -1
    public static final Integer STATUS_NEW = 0
    public static final Integer STATUS_ACTIVE = 1

    static hasMany = [roles: CrmUserRole, permissions: CrmUserPermission, options: CrmUserOption]

    static constraints = {
        username(size: 2..80, maxSize: 80, nullable: false, blank: false, unique: true)
        name(size: 2..80, maxSize: 80, nullable: false, blank: false)
        company(maxSize: 80, nullable: true)
        email(maxSize: 80, blank: false, email: true)
        telephone(size: 4..20, maxSize: 20, nullable: true)
        timezone(maxSize: 40, nullable: true)
        postalCode(size: 2..20, maxSize: 20, nullable: true)
        countryCode(size: 2..3, maxSize: 3, nullable: true)
        campaign(maxSize: 40, nullable: true)
        defaultTenant(nullable: true)
        status(inList: [STATUS_NEW, STATUS_ACTIVE, STATUS_BLOCKED, STATUS_CLOSED])
    }

    static mapping = {
        table 'crm_user'
        sort 'username'
        cache 'read-write'
        roles joinTable: [name: 'crm_user_role', key: 'user_id'], cascade: 'all-delete-orphan'
        permissions cascade: 'all-delete-orphan'
    }

    static transients = ['dao', 'created', 'enabled', 'blocked', 'timezoneInstance']

    static searchable = {
        only = ['username', 'email', 'name', 'company']
    }

    static List BIND_WHITELIST = [
            'username', 'email', 'name', 'company', 'telephone', 'timezone', 'postalCode', 'countryCode', 'campaign',
            'status', 'loginFailures', 'defaultTenant'
    ]

    def beforeValidate() {
        if (loginFailures != null && loginFailures > 9) {
            status = STATUS_BLOCKED
        }
    }

    /**
     * Returns the username property.
     * @return username property
     */
    @Override
    String toString() {
        username.toString()
    }

    transient boolean isCreated() {
        status == STATUS_NEW
    }

    transient boolean isEnabled() {
        status == STATUS_ACTIVE
    }

    transient boolean isBlocked() {
        status == STATUS_BLOCKED
    }

    /**
     * Clients should use this method to get user properties instead of accessing the domain instance directly.
     * The following properties are returned as a Map: [String guid, String username, String name, String email, String address1, String address2,
     * String postalCode, String city, String countryCode, String telephone, boolean enabled, boolean defaultTenant]
     * @return a data access object (Map) representing the domain instance.
     */
    transient Map<String, Object> getDao() {
        def tenant = TenantUtils.tenant
        def allPerm = []
        if (permissions) {
            allPerm.addAll(permissions.findAll { it.tenantId == tenant }.collect { it.toString() })
        }
        def allRoles = []
        for (role in roles.findAll { it.role.tenantId == tenant }) {
            allRoles << role.toString()
            def p = role.role.permissions
            if (p) {
                allPerm.addAll(p)
            }
        }
        def map = ['id', 'guid', 'username', 'email', 'name', 'company', 'telephone',
                'postalCode', 'countryCode', 'campaign', 'status', 'enabled', 'defaultTenant'].inject([:]) { m, i ->
            def v = this."$i"
            if (v != null) {
                m[i] = v
            }
            m
        }
        def tz = getTimezoneInstance()
        map.timezone = tz
        map.roles = allRoles
        map.permissions = allPerm
        map.options = getOptionsMap()
        return map
    }

    transient TimeZone getTimezoneInstance() {
        timezone ? TimeZone.getTimeZone(timezone) : TimeZone.getDefault()
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
                addToOptions(new CrmUserOption(key, value))
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

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        CrmUser that = (CrmUser) o

        if (username != that.username) return false
        if (email != that.email) return false
        if (name != that.name) return false

        return true
    }

    @Override
    int hashCode() {
        int result
        result = (username != null ? username.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (email != null ? email.hashCode() : 0)
        return result
    }
}
