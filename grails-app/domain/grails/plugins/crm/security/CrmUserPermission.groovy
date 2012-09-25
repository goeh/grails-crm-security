package grails.plugins.crm.security

import grails.plugins.crm.core.TenantEntity

/**
 * This domain class represents a user's permission within a specific account (tenant).
 * Normally users get permissions from it's roles (CrmRole.permissions) but users can also have individual permissions.
 * Individual permission are stored in this domain.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
@TenantEntity
class CrmUserPermission {

    String permissionsString

    static belongsTo = [user:CrmUser]

    static mapping = {
        table 'crm_user_permission'
        cache 'nonstrict-read-write'
    }

    String toString() {
        permissionsString.toString()
    }
}
