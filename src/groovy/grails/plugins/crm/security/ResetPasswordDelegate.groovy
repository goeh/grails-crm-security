/*
 * Copyright (c) 2014 Goran Ehrsson.
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

import groovy.transform.CompileStatic
import org.apache.log4j.Logger

/**
 * Delegate for the security-questions plugin that resets user password when forgotten.
 */
class ResetPasswordDelegate /* implements grails.plugins.resetpassword.ResetPasswordDelegate */ {

    def crmSecurityService
    def grailsApplication

    Logger log = Logger.getLogger(ResetPasswordDelegate.class)

    // @Override
    Map verifyAccount(Map params) {
        def user = CrmUser.findByUsername(params.username)
        if (user) {
            log.debug "resetPassword: Verifying account ${user.username} - ${user.name} ---> $params"
            for (field in grailsApplication.config.reset.password.step1.fields) {
                if (!equalsIgnoreSpace(user[field], params[field])) {
                    return null
                }
            }
            return [username: user.username, email: user.email, name: user.name]
        } else {
            log.warn "resetPassword: User [${params.username}] not found"
        }
        return null
    }

    @CompileStatic
    private boolean equalsIgnoreSpace(final String arg1, final String arg2) {
        if (arg1 && arg2) {
            return arg1.replaceAll(/\s/, '').equalsIgnoreCase(arg2.replaceAll(/\s/, ''))
        }
        return false
    }

    // @Override
    List<String> getQuestions(String username, List<String> questions) {
        return questions
    }

    // @Override
    boolean resetPassword(String username, String password) {
        log.debug "resetPassword: Changing password for [$username]"
        def user = crmSecurityService.getUser(username)
        if (user) {
            crmSecurityService.updateUser(user, [password: password, status: CrmUser.STATUS_ACTIVE, loginFailures: 0])
            return true
        }
        return false
    }

    // @Override
    boolean disableAccount(String username) {
        log.warn "resetPassword: Disabling account [$username]"
        def user = crmSecurityService.getUser(username)
        if (user) {
            crmSecurityService.updateUser(user, [status: CrmUser.STATUS_BLOCKED])
            return true
        }
        return false
    }
}
