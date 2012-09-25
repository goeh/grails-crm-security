package grails.plugins.crm.security

/**
 * System wide controller for security administration.
 */
class CrmSecurityAdminController {

    def index() {

        def namedPermissions = CrmNamedPermission.list()
        [namedPermissions:namedPermissions]
    }
}
