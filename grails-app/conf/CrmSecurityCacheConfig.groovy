import grails.plugins.crm.security.CrmTenantLog
import grails.plugins.crm.security.CrmUser
import grails.plugins.crm.security.CrmUserPermission
import grails.plugins.crm.security.CrmUserRole

config = {
    cache {
        name "permissions"
    }
    domain {
        name CrmUser
    }
    domain {
        name CrmUserRole
    }
    domain {
        name CrmUserPermission
    }
    domain {
        name CrmTenantLog
    }
}