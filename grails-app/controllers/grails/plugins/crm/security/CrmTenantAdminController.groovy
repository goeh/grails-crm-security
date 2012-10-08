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

import grails.plugins.crm.core.CrmException
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse

/**
 * Staff/admin controller for tenants.
 */
class CrmTenantAdminController {

    private static final List TENANT_BIND_WHITELIST = ['name', 'expires']

    def crmSecurityService
    def crmInvitationService
    def crmFeatureService

    def index() {
        def tenantStats = [:]
        def userStats = [:]
        tenantStats.total = CrmTenant.count()
        tenantStats.active = CrmTenant.createCriteria().count() {
            or {
                isNull('expires')
                ge('expires', new java.sql.Date(new Date().clearTime().time))
            }
        }
        userStats.total = CrmUser.count()
        userStats.active = CrmUser.countByEnabled(true)
        return [tenant: tenantStats, user: userStats]
    }

    def edit(Long id) {

        def crmTenant = CrmTenant.get(id)
        if (!crmTenant) {
            flash.error = message(code: 'crmTenant.not.found.message', args: [message(code: 'crmTenant.label', default: 'Account'), id])
            redirect action: 'index'
            return
        }

        def invitations = crmInvitationService ? crmInvitationService.getInvitationsFor(crmTenant, crmTenant.id) : []
        def allFeatures = crmFeatureService.applicationFeatures
        def features = crmFeatureService.getFeatures(crmTenant.id)
        def existingFeatureNames = features*.name
        def moreFeatures = allFeatures.findAll {
            if (it.hidden || existingFeatureNames.contains(it.name)) {
                return false
            }
            return true
        }
        def guestUsage = crmSecurityService.getRoleUsage('guest', id)
        def userUsage = crmSecurityService.getRoleUsage('user', id)
        def adminUsage = crmSecurityService.getRoleUsage('admin', id)

        switch (request.method) {
            case 'GET':
                return [crmTenant: crmTenant, user: crmTenant.user,
                        permissions: crmSecurityService.getTenantPermissions(crmTenant.id),
                        invitations: invitations, features: features, moreFeatures: moreFeatures,
                        guestUsage: guestUsage, userUsage: userUsage, adminUsage: adminUsage]
            case 'POST':
                if (params.version) {
                    def version = params.version.toLong()
                    if (crmTenant.version > version) {
                        crmTenant.errors.rejectValue('version', 'crmTenant.optimistic.locking.failure',
                                [message(code: 'crmTenant.label', default: 'Account')] as Object[],
                                "Another user has updated this account while you were editing")
                        render view: 'edit', model: [crmTenant: crmTenant, user: crmTenant.user,
                                permissions: crmSecurityService.getTenantPermissions(crmTenant.id),
                                invitations: invitations, features: features, moreFeatures: moreFeatures,
                                guestUsage: guestUsage, userUsage: userUsage, adminUsage: adminUsage]
                        return
                    }
                }

                bindData(crmTenant, params, [include: TENANT_BIND_WHITELIST])

                if (!crmTenant.save(flush: true)) {
                    render view: 'edit', model: [crmTenant: crmTenant, user: crmTenant.user,
                            permissions: crmSecurityService.getTenantPermissions(crmTenant.id),
                            invitations: invitations, features: features, moreFeatures: moreFeatures,
                            guestUsage: guestUsage, userUsage: userUsage, adminUsage: adminUsage]
                    return
                }

                flash.success = message(code: 'crmTenant.updated.message', args: [message(code: 'crmTenant.label', default: 'Account'), crmTenant.toString()])
                redirect action: 'edit', id: crmTenant.id
                break
        }
    }

    def reset(Long id) {
        crmSecurityService.resetPermissions(id)
        flash.warning = message(code: 'crmTenant.permissions.reset.message', default: "Permission reset")
        redirect action: 'edit', id: id
    }

    def delete(Long id) {
        def crmTenant = CrmTenant.get(id)
        if (!crmTenant) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        try {
            def tombstone = crmTenant.toString()
            crmSecurityService.deleteTenant(id)
            flash.warning = message(code: 'crmTenant.deleted.message', args: [message(code: 'crmTenant.label', default: 'Account'), tombstone])
            redirect action: 'index'
        } catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmTenant.not.deleted.message', args: [message(code: 'crmTenant.label', default: 'Account'), id])
            redirect action: 'edit', id: id
        } catch (CrmException crme) {
            flash.error = message(code: crme.message, args: crme.args)
            redirect action: 'edit', id: id
        }
    }

    def deletePermission(Long id) {
        def perm = CrmUserPermission.get(id)
        if (!perm) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        if (crmSecurityService.isCurrentUser(perm.user)) {
            flash.error = message(code: 'crmUserPermission.cannot.delete.self', default: 'You cannot delete your own permissions')
        } else {
            def user = perm.user
            user.removeFromPermissions(perm)
            //perm.delete(flush: true)
            user.save(flush: true)
            flash.warning = message(code: 'crmUserPermission.deleted.message', default: "Permission removed")
        }
        redirect action: "edit", id: params.tenant, fragment: 'permissions'
    }

    def deleteInvitation(Long id) {
        def crmInvitation = crmInvitationService.getInvitation(id)
        if (crmInvitation) {
            def label = crmInvitation.receiver
            crmInvitationService.cancel(crmInvitation.id)
            flash.warning = message(code: "crmInvitation.deleted.message", default: "Invitation to {0} deleted", args: [label])
        } else {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'crmInvitation.label', default: 'Invitation'), id])
        }
        redirect action: "edit", id: params.tenant, fragment: 'permissions'
    }
}
