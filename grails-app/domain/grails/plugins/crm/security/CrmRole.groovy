package grails.plugins.crm.security

import grails.plugins.crm.core.TenantEntity

/**
 * This domain class represents an account role.
 * Users can have multiple roles within an account.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
@TenantEntity
class CrmRole {

    String name
    String param
    String description
    static hasMany = [permissions: String]

    static constraints = {
        name(nullable: false, blank: false, unique: 'tenantId')
        param(maxSize:20, nullable:true)
        description(maxSize:255, nullable:true)
    }
    static mapping = {
        table 'crm_role'
        sort 'name'
        cache 'nonstrict-read-write'
        permissions joinTable: [name: 'crm_role_permission', key: 'role_id'], cascade: 'all-delete-orphan', cache: 'nonstrict-read-write'
    }

    @Override
    String toString() {
        return name.toString()
    }
}
