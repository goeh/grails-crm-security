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

/**
 * This domain class connect users and roles.
 * A user can have multiple roles in multiple accounts (tenants).
 * This design reaches beyond traditional multi-tenancy support where a user can only access one tenant.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
class CrmUserRole {

    CrmRole role
    java.sql.Date expires
    static belongsTo = [user: CrmUser]
    static constraints = {
        role(validator: { val, obj ->
            def accountExpires = CrmAccount.withNewSession { CrmTenant.get(obj.role?.tenantId)?.account?.expires }
            if (obj.expires && accountExpires && obj.expires > accountExpires) {
                return ['expires.after.account', obj.expires, accountExpires]
            }
            return null
        })
        expires(nullable: true)
    }
    static mapping = {
        table 'crm_user_role'
    }
    static transients = ['tenantId']

    /**
     * Return tenantId of the associated CrmRole.
     * This method exists primarily to make it easy to get Tenant ID from CrmUserRole and CrmUserPermission
     * in the same way, by calling getTenantId()
     * @return Tenant ID or null if role is null
     * @since 1.2.3
     */
    transient Long getTenantId() {
        role?.tenantId
    }

    String toString() {
        role.toString()
    }
}
