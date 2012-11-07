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
    def resetPasswordService

    static allowedMethods = [update: 'POST']

    def index() {
        def user = crmSecurityService.getCurrentUser()
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        def cmd = new CrmSettingsEditCommand()

        bindData(cmd, user.properties, [include: CrmUser.BIND_WHITELIST])

        getModel(cmd)
    }

    private Map getModel(CrmSettingsEditCommand cmd) {
        def crmUser = crmSecurityService.getCurrentUser()
        def options = crmUser.option
        def username = crmUser.username
        def questions = resetPasswordService?.getAvailableQuestions()
        def answers = resetPasswordService?.getQuestionsForUser(username)
        def tenants = crmSecurityService.getTenants(username)
        def timezones = TimeZone.getAvailableIDs().findAll { it.contains("Europe") }.collect { TimeZone.getTimeZone(it) }
        def currencies = ['SEK', 'NOK', 'EUR', 'GBP', 'USD'].collect { Currency.getInstance(it) }
        // TODO Make start-page contept dynamic so pages can be pluggable from Config.groovy
        // or better yet, from installed features.
        def startPages = ['start:index',
                'crmCalendar:index',
                'avtalaAgreement:dashboard', 'crmAgreement:index', 'crmAgreement:list',
                'crmContact:index', 'crmContact:list',
                'crmFolder:index', 'crmFolder:list'].inject([:]) { map, key ->
            map[key] = message(code: key.replace(':', '.') + '.label', default: key)
            return map
        }
        cmd.startPage = options?.startPage
        return [cmd: cmd, crmUser: crmUser, startPages: startPages,
                options: options, tenants: tenants,
                roles: crmUser.roles,
                questions: questions, answers: answers,
                timezones: timezones, currencies: currencies]
    }

    def update(CrmSettingsEditCommand cmd) {
        def user = crmSecurityService.getCurrentUser()
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        if (!cmd.validate()) {
            render(view: "index", model: getModel(cmd))
            return
        }

        bindData(user, params, [include: CrmUser.BIND_WHITELIST])

        user.setOption('startPage', cmd.startPage)

        if (user.save()) {
            def username = user.username

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
                    render(view: "index", model: getModel(cmd))
                    return
                }
            }

            flash.success = message(code: 'crmSettings.updated.message', default: "Settings updated")

            redirect(action: 'index')
        } else {
            log.error("Failed to save settings for user [${user.username}] ${user.errors}")
            flash.error = message(code: 'crmSettings.not.updated.message', default: "Settings could not be updated")
            render(view: "index", model: getModel(cmd))
        }
    }
}
