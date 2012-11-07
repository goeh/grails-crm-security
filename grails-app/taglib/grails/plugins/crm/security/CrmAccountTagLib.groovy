package grails.plugins.crm.security

/**
 * CrmAccount related tags.
 */
class CrmAccountTagLib {

    static namespace = "crm"

    def crmSecurityService

    def accountQuotaGreaterThan = { attrs, body ->
        def account = crmSecurityService.getCurrentAccount()
        if (account) {
            def quota = attrs.quota
            if (!quota) {
                throwTagError("Tag [accountQuotaGreaterThan] is missing required attribute [quota]")
            }
            def reference = Integer.valueOf(attrs.value ?: 0)
            def value = account?.getOption(quota) ?: 0
            if (value > reference) {
                out << body()
            }
        }
    }

    def isAllowedMoreTenants = {attrs, body->
        def account = crmSecurityService.getCurrentAccount()
        if (account) {
            def max = account?.getOption('maxTenants') ?: 0
            def size = account.tenants?.size() ?: 0
            if(size < max) {
                out << body()
            }
        }
    }

}
