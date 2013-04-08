package grails.plugins.crm.security

import grails.plugins.crm.core.DateUtils

import javax.servlet.http.HttpServletResponse

/**
 * Account admin.
 */
class CrmAccountAdminController {
    static allowedMethods = [index: 'GET', edit: ['GET', 'POST'], delete: 'POST', transferTenant: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 75,
                    title: 'crmAccount.index.label',
                    action: 'index'
            ]
    ]

    def crmSecurityService
    def crmAccountService

    def index() {
        def recent = CrmAccount.createCriteria().list() {
            order 'dateCreated', 'desc'
            maxResults 10
        }
        [recent: recent]
    }

    def edit(Long id) {
        def user = crmSecurityService.getUserInfo()
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        def crmAccount = CrmAccount.get(id)
        if (!crmAccount) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        switch (request.method) {
            case "GET":
                break
            case "POST":
                def date = params.remove('expires')
                bindData(crmAccount, params, [include: CrmAccount.BIND_WHITELIST])
                bindDate(crmAccount, 'expires', date, user.timezone)
                crmAccount.setStatusText(params.status)
                for (item in ['crmTenant', 'crmAdmin', 'crmUser', 'crmGuest', 'crmContent']) {
                    crmAccount.setItem(item, params.int(item))
                }
                if (crmAccount.validate() && crmAccount.save()) {
                    if (params.transfer) {
                        transferTenant(id, params.long('transfer'))
                        redirect action: 'edit', id: id
                    } else {
                        flash.success = message(code: 'crmAccount.updated.message', default: "Account updated")
                        redirect(action: 'edit', id: id)
                    }
                    return
                }
                break
        }
        def transfers = CrmTenant.createCriteria().list() {
            isNotNull('transfer')
            ne('account', crmAccount)
        }
        [crmAccount: crmAccount, options: crmAccount.option, transfers: transfers,
                roles: crmAccountService.getRoleStatistics(crmAccount), statusList: buildStatusList()]
    }

    private List<String> buildStatusList() {
        CrmAccount.constraints.status.inList.collect {
            def dummy = new CrmAccount()
            dummy.status = it
            def code = dummy.getStatusText()
            dummy.discard()
            return code
        }
    }

    private void bindDate(def target, String property, String value, TimeZone timezone = null) {
        if (value) {
            try {
                target[property] = DateUtils.parseSqlDate(value, timezone)
            } catch (Exception e) {
                def entityName = message(code: 'crmAccount.label', default: 'Subscription')
                def propertyName = message(code: 'crmAccount.' + property + '.label', default: property)
                target.errors.rejectValue(property, 'default.invalid.date.message', [propertyName, entityName, value.toString(), e.message].toArray(), "Invalid date: {2}")
            }
        } else {
            target[property] = null
        }
    }

    def delete(Long id) {
        def crmAccount = CrmAccount.get(id)
        if (!crmAccount) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        crmAccountService.closeAccount(id)

        def crmUser = crmAccount.user
        flash.warning = message(code: 'crmAccount.deleted.message', args: [crmAccount.toString(), crmUser.email])
        redirect(action: 'edit', id: id)
    }

    private void transferTenant(Long id, Long tenant) {
        def crmAccount = CrmAccount.get(id)
        if (!crmAccount) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        if (!crmTenant.transfer) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }
        // Do the transfer
        crmAccountService.transferTenant(crmTenant.ident(), crmAccount)

        flash.warning = message(code: 'crmTenant.transfer.complete.message', args: [crmTenant.toString(), crmAccount.toString()])
    }
}
