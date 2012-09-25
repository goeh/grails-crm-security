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

import grails.events.Listener
import grails.plugins.crm.core.TenantUtils
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

/**
 * A service that listens to tenant sharing events.
 */
class CrmTenantSharingService {

    def crmSecurityService
    def crmInvitationService

    LinkGenerator grailsLinkGenerator

    /**
     * A user wants to share an account with another user.
     * Send email invitation to other user.
     *
     * @param event
     */
    @Listener(namespace = "crm", topic = "tenantShared")
    def tenantShared(info) {
        crmSecurityService.runAs(info.user) {
            TenantUtils.withTenant(info.id) {
                def user = crmSecurityService.getUserInfo(info.user)
                def tenant = crmSecurityService.getTenantInfo(info.id)
                def email = info.email
                def message = info.message

                log.info "${user.name} wants to share tenant ${tenant.name} with $email ($message)"

                def binding = [target: tenant, user: user, email: email, role: info.role, message: message]

                binding.url = grailsLinkGenerator.link(mapping: 'crm-register', params: [email: email], absolute: true)
                def reference = "crmTenant@" + tenant.id // TODO it's bad to manually construct a reference identifier!
                crmInvitationService.createInvitation(reference, info.role, user.username, email, "account-invite-email", binding)
            }
        }
    }

    @Listener(namespace = "crm", topic = "invitationAccepted")
    def invitationAccepted(invitation) {
        def invitedUser = CrmUser.findByEmail(invitation.receiver)
        if (invitedUser) {
            def tenant = invitation.tenantId
            def role = invitation.param
            log.debug("${invitation.sender} is inviting user $invitedUser to tenant $tenant as $role")
            if (tenant && role) {
                crmSecurityService.runAs(invitation.sender) {
                    TenantUtils.withTenant(tenant) {
                        crmSecurityService.addUserRole(invitedUser.username, role, null/*DateUtils.endOfWeek(30)*/)
                        if (!invitedUser.defaultTenant) {
                            invitedUser.discard()
                            invitedUser = CrmUser.lock(invitedUser.id)
                            invitedUser.defaultTenant = tenant
                            invitedUser.save(flush: true)
                        }
                    }
                }
            }
        } else {
            log.error("Accepting user [${invitation.receiver}] not found for invitation [${invitation.id}]")
        }
    }
}
