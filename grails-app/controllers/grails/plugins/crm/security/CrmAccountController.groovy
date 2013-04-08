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

import javax.servlet.http.HttpServletResponse

/**
 * CRM Account Management.
 */
class CrmAccountController {

    static allowedMethods = [index: 'GET', delete: 'POST']

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

    def index() {
        def user = crmSecurityService.getCurrentUser()
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        def allAccounts = crmAccountService.getAccounts(user.username)
        if (allAccounts.isEmpty()) {
            redirect action: "create"
        } else if (allAccounts.size() == 1) {
            redirect action: "edit", id: allAccounts.head().id
        } else {
            redirect action: "list"
        }
    }

    def edit(Long id) {
        def user = crmSecurityService.getCurrentUser()
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        def crmAccount = CrmAccount.get(id)
        if (!crmAccount) {
            redirect action: "index"
            return
        }
        if (crmAccount.user.id != user.id) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        switch (request.method) {
            case "GET":
                break
            case "POST":
                bindData(crmAccount, params, [include: CrmAccount.BIND_WHITELIST])

                if (crmAccount.save()) {
                    flash.success = message(code: 'crmAccount.updated.message', default: "Account updated")
                    redirect(action: 'edit', id: crmAccount.id)
                    return
                }
                break
        }
        def allAccounts = crmAccountService.getAccounts(user.username)
        [user: user, accountList: allAccounts, crmAccount: crmAccount, options: crmAccount.option,
                tenantList: crmAccount.tenants, roles: crmAccountService.getRoleStatistics(crmAccount)]
    }

    def list() {
        def user = crmSecurityService.getCurrentUser()
        if (user) {
            return [crmUser: user, accountList: crmAccountService.getAccounts(user.username)]
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
    }

    def create() {
        def user = crmSecurityService.getCurrentUser()

        switch (request.method) {
            case "GET":
                return [crmUser: user, crmAccount: new CrmAccount()]
                break
            case "POST":
                def crmTenant = crmAccountService.createTrialAccount(user, params, request.getLocale())
                flash.success = message(code: 'crmAccount.updated.message', default: "Account updated")
                redirect(action: 'edit', id: crmTenant.account.id)
                break
        }
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
            crmSecurityService.crmAccountService(id)
            flash.warning = message(code: 'crmAccount.deleted.message', args: [crmAccount.toString(), crmUser.email])
            redirect mapping: "logout"
        } else {
            return [crmAccount: crmAccount, user: crmUser]
        }
    }
}
