package grails.plugins.crm.security

/**
 * A named permission groups a set of permissions under a common name.
 */
class CrmNamedPermission {
    String name

    static hasMany = [permissions: String]

    static constraints = {
        name(maxSize: 80, blank: false, unique: true)
    }

    static mapping = {
        table 'crm_named_permission'
        sort 'name'
        cache 'nonstrict-read-write'
        permissions joinTable: [name: 'crm_named_permission_string', key: 'name_id'], cache: 'nonstrict-read-write'
    }

    String toString() {
        name.toString()
    }
}
