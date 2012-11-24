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

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import javax.servlet.http.HttpServletResponse
import grails.plugins.crm.core.TenantUtils

/**
 * User Registration.
 */
class CrmRegisterController {

    def grailsApplication
    def simpleCaptchaService
    def crmSecurityService

    def index(RegisterUserCommand cmd) {

        switch (request.method) {
            case "POST":
                if (cmd.hasErrors()) {
                    return [cmd: cmd]
                }
                if (!simpleCaptchaService.validateCaptcha(params.captcha)) {
                    cmd.errors.rejectValue("captcha", "captcha.invalid.message")
                    return [cmd: cmd]
                }
                def success = false
                withForm {
                    try {
                        def props = cmd.toMap()
                        props.ip = request.remoteAddr // Save IP address if we need to track abuse.
                        crmSecurityService.createUser(props)
                        success = true
                    } catch (Exception e) {
                        log.error("Could not create user ${cmd.name} (${cmd.username}) <${cmd.email}>", e)
                        flash.error = message(code: 'register.error.message', default: "Failed to register, please try again later", args: [e.message])
                    }
                }.invalidToken {
                    cmd.clearErrors()
                    flash.error = "form.submit.twice"
                    flash.defaultMessage = "Form submitted twice!"
                }
                if (success) {
                    render(view: 'verify', model: [user: cmd])
                    return
                }
                break
            case "GET":
                if (params.c) {
                    request.session.crmRegisterCampaign = params.c
                    if (!cmd.campaign) {
                        cmd.campaign = params.c
                    }
                }
                cmd.clearErrors()
                break
        }

        return [cmd: cmd]
    }

    def verify() {

    }

    def confirm(String id) {
        def user = CrmUser.findByGuid(id)
        if (user) {
            def userInfo = crmSecurityService.updateUser(user.username, [status: CrmUser.STATUS_ACTIVE])
            def targetUri = grailsApplication.config.crm.register.welcome.url ?: "/welcome"
            return [user: userInfo, targetUri: targetUri]
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }
}
