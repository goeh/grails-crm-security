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
            def max = account.getItem('crmTenant')?.quantity ?: 1
            def size = account.tenants?.size() ?: 0
            if(size < max) {
                out << body()
            }
        }
    }

    def isAllowedMoreInvitations = {attrs, body->
            def account = crmSecurityService.getCurrentAccount()
            if (account) {
                def stats = crmSecurityService.getRoleStatistics(account)
                def maxAdmins = account.getItem('crmAdmin')?.quantity ?: 1
                def maxUsers = account.getItem('crmUser')?.quantity ?: 0
                def maxGuests = account.getItem('crmGuest')?.quantity ?: 0
                def currentAdmins = stats.admin?.size() ?: 0
                def currentUsers = stats.user?.size() ?: 0
                def currentGuests = stats.guest?.size() ?: 0
                def moreAdmins = maxAdmins - currentAdmins
                def moreUsers = maxUsers - currentUsers
                def moreGuests = maxGuests - currentGuests
                def ok = false
                switch(attrs.role) {
                    case "admin":
                        ok = moreAdmins > 0
                        break
                    case "user":
                        ok = moreUsers > 0
                        break
                    case "guest":
                        ok = moreGuests > 0
                        break
                    default:
                        ok = (moreAdmins + moreUsers + moreGuests) > 0
                        break
                }
                if(ok) {
                    out << body()
                }
            }
        }

}
