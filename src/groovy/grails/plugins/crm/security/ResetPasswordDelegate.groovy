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

import org.apache.log4j.Logger

/**
 * Delegate for the security-questions plugin that resets user password when forgotten.
 */
class ResetPasswordDelegate {

    def crmSecurityService
    def grailsApplication

    Logger log = Logger.getLogger(ResetPasswordDelegate.class)

    def verifyAccount(Map params) {
        def user = CrmUser.findByUsername(params.username)
        if (user) {
            log.debug "resetPassword: Verifying account ${user.username} - ${user.name} ---> $params"
            if (!user.email.equalsIgnoreCase(params.email)) {
                return null
            }
            if (grailsApplication.config.reset.password.step1.fields?.contains('postalCode')
                    && !equalsIgnoreSpace(user.postalCode, params.postalCode)) {
                return null
            }
            return user.username
        } else {
            log.warn "resetPassword: User [${params.username}] not found"
        }
        return null
    }

    private boolean equalsIgnoreSpace(String arg1, String arg2) {
        if (arg1 && arg2) {
            return arg1.replaceAll(/\s/, '').equalsIgnoreCase(arg2.replaceAll(/\s/, ''))
        }
        return false
    }

    def getQuestions(username, questions) {
        return questions
    }

    def resetPassword(String username, String password) {
        log.debug "resetPassword: Changing password for [$username]"
        crmSecurityService.updateUser(username, [password: password, enabled:true, loginFailures: 0])
        return username
    }

    def disableAccount(String username) {
        log.warn "resetPassword: Disabling account [$username]"
        def user = CrmUser.findByUsername(username)
        if (user) {
            user.enabled = false
            user.save()
        }
    }
}
