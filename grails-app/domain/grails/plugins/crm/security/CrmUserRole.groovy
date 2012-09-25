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
        role()
        expires(nullable:true)
    }
    static mapping = {
        table 'crm_user_role'
    }
    String toString() {
        role.toString()
    }
}
