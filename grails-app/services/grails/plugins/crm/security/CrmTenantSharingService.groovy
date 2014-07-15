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

import grails.events.Listener
import grails.plugins.crm.core.CrmException
import grails.plugins.crm.core.DateUtils
import grails.plugins.crm.core.TenantUtils
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

/**
 * A service that listens to tenant sharing events.
 */
class CrmTenantSharingService {

    def crmSecurityService
    def crmInvitationService
    def grailsApplication

    LinkGenerator grailsLinkGenerator

    /**
     * A user wants to share an account with another user.
     * Send email invitation to other user.
     *
     * @param event
     */
    @Listener(namespace = "crmTenant", topic = "share")
    def tenantShared(info) {
        crmSecurityService.runAs(info.user) {
            def crmTenant = crmSecurityService.getTenant(info.id)
            TenantUtils.withTenant(crmTenant.id) {
                def email = info.email
                def message = info.message

                log.info "${info.user} wants to share tenant ${crmTenant.name} with $email ($message)"

                def binding = [:]
                binding.role = info.role
                binding.message = message
                binding.registerUrl = grailsLinkGenerator.link(mapping: 'crm-register', params: [email: email], absolute: true)
                binding.loginUrl = grailsLinkGenerator.link(mapping: 'login', params: [username: email], absolute: true)
                // Check if the invited email is already a registered user.
                binding.existing = CrmUser.createCriteria().count() {
                    ilike('email', email)
                }
                crmInvitationService.createInvitation(crmTenant, info.role, info.user, email, binding)
            }
        }
    }

    @Listener(namespace = "crmInvitation", topic = "accepted")
    def invitationAccepted(invitation) {
        if (!invitation.ref?.startsWith('crmTenant@')) {
            return
        }
        def invitedUser = CrmUser.findByEmail(invitation.receiver)
        if (invitedUser) {
            def tenant = invitation.tenantId
            def role = invitation.param
            log.debug("${invitation.sender} is inviting user $invitedUser to tenant $tenant as $role")
            if (tenant && role) {
                crmSecurityService.runAs(invitation.sender, tenant) {
                    def expires = grailsApplication.config.crm.permission.expires ?: null
                    try {
                        crmSecurityService.addUserRole(invitedUser, role, expires ? DateUtils.endOfWeek(expires) : null)
                        if (!invitedUser.defaultTenant) {
                            //invitedUser.discard()
                            //invitedUser = CrmUser.lock(invitedUser.id)
                            invitedUser.defaultTenant = tenant
                        }
                        invitedUser.save(flush: true)
                    } catch (CrmException e) {
                        log.error("Invitation failed for [${invitedUser.username}]", e)
                    } catch (Exception e) {
                        log.error("Invitation failed for [${invitedUser.username}]", e)
                    }
                }
            }
        } else {
            log.error("Accepting user [${invitation.receiver}] not found for invitation [${invitation.id}]")
        }
    }
}
