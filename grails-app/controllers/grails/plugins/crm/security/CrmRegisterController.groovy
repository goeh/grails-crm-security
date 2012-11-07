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
    def textTemplateService

    def crmSecurityService

    LinkGenerator grailsLinkGenerator


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
                        def user = crmSecurityService.createUser(props)
                        TenantUtils.withTenant(1) {
                            sendVerificationEmail(user.dao)
                        }
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

    private void sendVerificationEmail(params) {
        def config = grailsApplication.config.crm.register.email
        def binding = params as Map
        binding.url = grailsLinkGenerator.link(controller: controllerName, action: 'confirm', id: params.guid, absolute: true)
        def bodyText = textTemplateService.applyTemplate("register-verify-email", "text/plain", binding)
        def bodyHtml = textTemplateService.applyTemplate("register-verify-email", "text/html", binding)
        if (!(bodyText || bodyHtml)) {
            throw new RuntimeException("Template not found: [name=register-verify-email]")
        }
        sendMail {
            if (bodyText && bodyHtml) {
                multipart true
            }
            if (config.from) {
                from config.from
            }
            to params.email
            if (config.cc) {
                cc config.cc
            }
            if (config.bcc) {
                bcc config.bcc
            }
            subject config.subject ?: "Confirm registration"
            if (bodyText) {
                text bodyText
            }
            if (bodyHtml) {
                html bodyHtml
            }
        }
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
