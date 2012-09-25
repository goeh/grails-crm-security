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

class CrmSettingsController {

    def crmSecurityService
    def userSettingsService
    def resetPasswordService

    private static final List USER_BIND_WHITELIST = ['name', 'email', 'company', 'address1', 'address2', 'postalCode', 'city', 'countryCode', 'telephone', 'currency']

    def index() {
        getModel(crmSecurityService.getUser())
    }

    private Map getModel(user) {
        def tenants = crmSecurityService.getAllTenants()
        def current = crmSecurityService.currentTenant

        // Load user settings
        def settings = [:]
        def username = user.username
        def questions = resetPasswordService?.getAvailableQuestions()
        def answers = resetPasswordService?.getQuestionsForUser(username)
        settings.favoritesMenu = userSettingsService?.getValue(username, "favoritesMenu") ?: 'top'
        settings.selectionsMenu = userSettingsService?.getValue(username, "selectionsMenu") ?: 'top'
        return [user: user, settings: settings, questions: questions, answers: answers,
                tenantList: tenants.collect {crmSecurityService.getTenantInfo(it) + [current: current?.id == it]}]
    }

    def update() {
        def user = crmSecurityService.getUser()
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }

        bindData(user, params, [include: USER_BIND_WHITELIST])

        if (user.save()) {
            def username = user.username
            if(userSettingsService) {
                userSettingsService.update(username, "favoritesMenu", params.favoritesMenu)
                userSettingsService.update(username, "selectionsMenu", params.selectionsMenu)
            }
            if (resetPasswordService) {
                def qa = [:]
                10.times {
                    def q = params['q[' + it + ']']
                    def a = params['a[' + it + ']']
                    if (q && a) {
                        qa[q] = a
                    }
                }
                if (qa) {
                    resetPasswordService.setAnswers(username, qa)
                }
            }
            if (params.password1) {
                if (params.password1 == params.password2) {
                    crmSecurityService.updateUser(username, [password: params.password1])
                } else {
                    flash.error = message(code: 'crmSettings.password.not.equal.message', default: "Passwords were not equal")
                    render(view: "index", model: getModel(user))
                    return
                }
            }

            flash.success = message(code: 'crmSettings.updated.message', default: "Settings updated")

            redirect(action: 'index')
        } else {
            log.error("Failed to save settings for user [${user.username}] ${user.errors}")
            flash.error = message(code: 'crmSettings.not.updated.message', default: "Settings could not be updated")
            render(view: "index", model: getModel(user))
        }
    }
}
