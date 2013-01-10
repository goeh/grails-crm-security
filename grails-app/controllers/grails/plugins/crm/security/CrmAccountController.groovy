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

import grails.plugins.crm.core.DateUtils

import javax.servlet.http.HttpServletResponse

/**
 * CRM Account Management.
 */
class CrmAccountController {
    static navigation = [
            [group: 'settings',
                    order: 53,
                    title: 'crmAccount.index.label',
                    action: 'index'
            ]
    ]

    def grailsApplication
    def crmSecurityService
    def crmAccountService

    private Date getExpiryDate(Locale locale) {
        def trialDays = grailsApplication.config.crm.account.trialDays ?: 30
        def cal = Calendar.getInstance(locale)
        cal.clearTime()
        cal.add(Calendar.DAY_OF_MONTH, trialDays)
        return DateUtils.endOfWeek(0, cal.getTime()) // Be nice and expire on a Sunday.
    }

    def index() {
        def user = crmSecurityService.getCurrentUser()
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        def crmAccount = crmSecurityService.getCurrentAccount()
        if (!crmAccount) {
            redirect(mapping: 'crm-account-create')
            return
        }

        switch (request.method) {
            case "GET":
                break
            case "POST":
                bindData(crmAccount, params, [include: CrmAccount.BIND_WHITELIST])

                if (crmAccount.save()) {
                    flash.success = message(code: 'crmAccount.updated.message', default: "Account updated")
                    redirect(action: 'index')
                    return
                }
                break
        }

        [crmAccount: crmAccount, options: crmAccount.option, roles: crmSecurityService.getRoleStatistics(crmAccount)]
    }

    def delete(Long id) {
        def crmAccount = CrmAccount.get(id)
        if (!crmAccount) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        def crmUser = crmSecurityService.getCurrentUser()
        if (!crmUser) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        if (crmAccount.user != crmUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        if (request.method == 'POST') {
            crmAccount.status = CrmAccount.STATUS_CLOSED
            crmAccount.save()
            crmUser.status = CrmUser.STATUS_CLOSED
            crmUser.save(flush: true)

            flash.warning = message(code: 'crmAccount.deleted.message', args: [crmAccount.toString(), crmUser.email])
            redirect mapping: "logout"
        } else {
            return [crmAccount: crmAccount, user: crmUser]
        }
    }
}
