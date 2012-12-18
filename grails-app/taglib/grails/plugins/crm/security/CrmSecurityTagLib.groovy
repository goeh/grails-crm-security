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

import grails.plugins.crm.core.TenantUtils

/**
 * CRM Account related tag libraries.
 */
class CrmSecurityTagLib {

    static namespace = "crm"

    def crmSecurityService

    /**
     * Render tag body if no user is authenticated.
     *
     * @attr username render tag body if user exists
     */
    def noUser = { attrs, body ->
        def username = attrs.username ?: crmSecurityService?.getCurrentUser()?.username
        def principal = username ? crmSecurityService?.getUserInfo(username) : null
        if (!principal) {
            out << body()
        }
    }

    /**
     * Render tag body if user is authenticated. Tag body can access properties
     * returned from Map crmSecurityService#getUserInfo(String)
     *
     * @attr username render tag body if user exists
     */
    def user = { attrs, body ->
        def username = attrs.username ?: crmSecurityService?.getCurrentUser()?.username
        def principal = username ? crmSecurityService?.getUserInfo(username) : null
        if (principal) {
            out << body(principal as Map)
        } else if (attrs.nouser) {
            out << attrs.nouser.toString()
        }
    }

    def noTenant = { attrs, body ->
        def tenant = crmSecurityService?.getCurrentTenant()
        if (!tenant) {
            out << body()
        }
    }

    def tenant = { attrs, body ->
        CrmTenant.withTransaction {
            def tenant = crmSecurityService?.getCurrentTenant()
            if (tenant) {
                out << body(tenant.dao)
            }
        }
    }

    def eachTenant = { attrs, body ->
        def list = crmSecurityService?.getTenants()
        list.eachWithIndex { s, i ->
            def map = [(attrs.var ?: 'it'): s]
            if (attrs.status) {
                map[attrs.status] = i
            }
            out << body(map)
        }
    }

    def hasPermission = { attrs, body ->
        def perm = attrs.permission
        if (!perm) {
            throwTagError("Tag [hasPermission] is missing required attribute [permission]")
        }
        if (crmSecurityService?.isPermitted(perm)) {
            out << body()
        }
    }

    def permissionList = { attrs, body ->
        def permissions = attrs.permission ?: attrs.permissions
        if (!(permissions instanceof Collection)) {
            permissions = [permissions]
        }
        int i = 0
        for (p in permissions) {
            def map = [(attrs.var ?: 'it'): [label: message(code: p, default: p), permission: p]]
            if (attrs.status) {
                map[attrs.status] = i++
            }
            out << body(map)
        }
    }

    def userRoles = { attrs, body ->
        def tenant = attrs.tenant ?: TenantUtils.tenant
        def username = attrs.username ?: crmSecurityService.getCurrentUser()?.username
        def result = CrmUserRole.createCriteria().list() {
            role {
                eq('tenantId', tenant)
            }
            user {
                eq('username', username)
            }
        }.collect {
            def role = it.role
            [role: role.name, expires: it.expires, param: role.param, permissions: role.permissions.flatten()]
        }
        int i = 0
        for (r in result) {
            def map = [(attrs.var ?: 'it'): r]
            if (attrs.status) {
                map[attrs.status] = i++
            }
            out << body(map)
        }
    }

    def userPermissions = { attrs, body ->
        def tenant = attrs.tenant ?: TenantUtils.tenant
        def username = attrs.username ?: crmSecurityService.getCurrentUser()?.username
        def result = CrmUserPermission.createCriteria().list() {
            projections {
                property('permissionsString')
            }
            eq('tenantId', tenant)
            user {
                eq('username', username)
            }
        }
        int i = 0
        for (p in result) {
            def map = [(attrs.var ?: 'it'): p]
            if (attrs.status) {
                map[attrs.status] = i++
            }
            out << body(map)
        }
    }

    /**
     * Render value of tenant option.
     *
     * @attr name REQUIRED name of option
     * @attr tenant tenant id (defaults to current tenant)
     * @attr eq render tag body if tenant option is equal the attribute value
     * @attr ne render tag body if tenant option is NOT equal the attribute value
     */
    def tenantOption = { attrs, body ->
        def option = attrs.name
        if (!option) {
            out << "Tag [tenantOption] missing required attribute [name]"
            return
        }
        def id = attrs.tenant ? Long.valueOf(attrs.tenant.toString()) : TenantUtils.tenant
        def tenant = crmSecurityService?.getTenant(id)
        if (tenant) {
            def value = tenant.getOption(option)
            def render = false
            if (attrs.eq && (attrs.eq == value)) {
                render = true
            } else if (attrs.ne && (attrs.ne != value)) {
                render = true
            } else if (value) {
                render = true
            }
            if (render) {
                out << body()
            }
        }
    }

}
