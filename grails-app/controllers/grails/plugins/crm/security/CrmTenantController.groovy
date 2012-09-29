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
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.CrmException
import grails.plugins.crm.core.DateUtils

/**
 * This controller lets a user manage her account/tenant.
 */
class CrmTenantController {

    static allowedMethods = [index: 'GET', create: ['GET', 'POST'], activate: ['GET', 'POST']]

    static navigation = [
            [group: 'settings',
                    order: 30,
                    title: 'crmTenant.permissions.label',
                    action: 'permissions'
            ]
    ]

    private static final List TENANT_BIND_WHITELIST = ['name']

    def crmSecurityService
    def crmInvitationService
    def crmFeatureService

    private boolean checkPermission(grails.plugins.crm.security.CrmTenant account) {
        account.user.guid == crmSecurityService.currentUser?.guid
    }

    def index() {
        def user = crmSecurityService.currentUser
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        [crmUser: user, crmTenantList: crmSecurityService.tenants]
    }

    def activate(Long id) {
        if (crmSecurityService.isValidTenant(id)) {
            def oldTenant = TenantUtils.getTenant()
            if (id != oldTenant) {
                TenantUtils.setTenant(id)
                request.session.tenant = id
                event(for: "crm", topic: "tenantChanged", data: [newTenant: id, oldTenant: oldTenant, request: request])
            }
            if (params.referer) {
                redirect(uri: params.referer - request.contextPath)
            } else {
                redirect(mapping: 'start')
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
        }
    }

    def create() {
        def crmUser = crmSecurityService.getUser()
        if (!crmUser) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        def crmTenant = new CrmTenant()
        bindData(crmTenant, params, [include: ['name', 'options']])

        switch (request.method) {
            case 'GET':
                crmTenant.clearErrors()
                break
            case 'POST':
                crmTenant.user = crmUser // To get validate to pass user must be set.
                if (crmTenant.validate()) {
                    try {
                        def options = [locale: RCU.getLocale(request)]
                        def trialDays = grailsApplication.config.crm.tenant.trialDays
                        if (trialDays) {
                            options.expires = new java.sql.Date(DateUtils.endOfWeek(trialDays).time)
                        }
                        def tenant = crmSecurityService.createTenant(crmTenant.name, options)
                        def id = tenant.id
                        if (!TenantUtils.tenant) {
                            // No active tenant, set the newly created tenant as active.
                            TenantUtils.setTenant(id)
                            request.session.tenant = id
                        }
                        if (params.boolean('defaultTenant')) {
                            crmSecurityService.updateUser(crmUser.username, [defaultTenant: id])
                        }
                        def features = params.list('features')
                        for (f in features) {
                            def appFeature = crmFeatureService.getApplicationFeature(f)
                            if (appFeature && !appFeature.enabled) {
                                crmFeatureService.enableFeature(f, id)
                            }
                        }
                        def installedFeatures = features.collect {
                            g.message(code: 'feature.' + it + '.label', default: it)
                        }
                        flash.success = message(code: 'crmTenant.created.message', args: [message(code: 'crmTenant.label', default: 'Account'),
                                tenant.name, installedFeatures.join(', ')])
                        redirect(action: "index")
                        return
                    } catch (Exception e) {
                        log.error(e)
                        flash.error = message(code: 'crmTenant.error', args: [e.message])
                    }
                }
                break
        }
        return [crmUser: crmUser, crmTenant: crmTenant, features: [], allFeatures: crmFeatureService.applicationFeatures]
    }

    def edit() {

        def crmTenant = CrmTenant.get(params.id)
        if (!crmTenant) {
            flash.error = message(code: 'crmTenant.not.found.message', args: [message(code: 'crmTenant.label', default: 'Account'), params.id])
            redirect action: 'index'
            return
        }
        if (!checkPermission(crmTenant)) {
            flash.error = message(code: 'crmTenant.permission.denied', args: [message(code: 'crmTenant.label', default: 'Account'), params.id])
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
        def showCosts = crmTenant.getOption('agreement.costs')
        def partner2 = crmTenant.getOption('agreement.partner2')

        switch (request.method) {
            case 'GET':
                return [crmTenant: crmTenant, user: crmSecurityService.currentUser,
                        permissions: crmSecurityService.getTenantPermissions(crmTenant.id),
                        showCosts: showCosts, partner2: partner2, invitationList: invitations, features: features, moreFeatures: moreFeatures]
            case 'POST':
                if (params.version) {
                    def version = params.version.toLong()
                    if (crmTenant.version > version) {
                        crmTenant.errors.rejectValue('version', 'crmTenant.optimistic.locking.failure',
                                [message(code: 'crmTenant.label', default: 'Account')] as Object[],
                                "Another user has updated this account while you were editing")
                        render view: 'edit', model: [crmTenant: crmTenant, user: crmSecurityService.currentUser]
                        return
                    }
                }

                bindData(crmTenant, params, [include: TENANT_BIND_WHITELIST])

                if (!crmTenant.save(flush: true)) {
                    render view: 'edit', model: [crmTenant: crmTenant, user: crmSecurityService.currentUser,
                            permissions: crmSecurityService.getTenantPermissions(crmTenant.id),
                            showCosts: showCosts, partner2: partner2, invitationList: invitations, features: features, moreFeatures: moreFeatures]
                    return
                }

                def defaultTenant = params.boolean('defaultTenant')
                if (defaultTenant) {
                    crmSecurityService.updateUser(null, [defaultTenant: crmTenant.id])
                }

                if (params.boolean('showCosts')) {
                    crmTenant.setOption('agreement.costs', true)
                } else {
                    crmTenant.removeOption('agreement.costs')
                }
                if (params.boolean('partner2')) {
                    crmTenant.setOption('agreement.partner2', true)
                } else {
                    crmTenant.removeOption('agreement.partner2')
                }

                flash.success = message(code: 'crmTenant.updated.message', args: [message(code: 'crmTenant.label', default: 'Account'), crmTenant.toString()])
                redirect action: 'index'
                break
        }
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

    def permissions() {
        def id = TenantUtils.tenant
        def crmTenant = CrmTenant.get(id)
        if (!crmTenant) {
            flash.error = message(code: 'crmTenant.not.found.message', args: [message(code: 'crmTenant.label', default: 'Account'), id])
            redirect action: 'index'
            return
        }
        def invitations = crmInvitationService ? crmInvitationService.getInvitationsFor(crmTenant, crmTenant.id) : []
        def currentUser = crmSecurityService.getUser()
        return [me: currentUser, crmTenant: crmTenant, permissions: crmSecurityService.getTenantPermissions(crmTenant.id),
                invitations: invitations]
    }

    def deleteRole(Long id) {
        def role = CrmUserRole.get(id)
        if (!role) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        if (crmSecurityService.isCurrentUser(role.user)) {
            flash.error = message(code: 'crmUserRole.cannot.delete.self', default: 'You cannot delete your own roles')
        } else {
            def user = role.user
            user.removeFromRoles(role)
            //role.delete(flush: true)
            user.save(flush: true)
            flash.warning = message(code: 'crmUserRole.deleted.message', default: "Role deleted")
        }
        redirect action: 'permissions'
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
        redirect action: 'permissions'
    }

    def deleteInvitation(Long id) {
        def crmInvitation = crmInvitationService.getInvitation(id)
        if (crmInvitation) {
            def user = crmSecurityService.currentUser
            if (crmInvitation.sender != user.username) {
                log.warn("Invalid user [${user.username}] trying to cancel invitation [${crmInvitation.id}] for [${crmInvitation.receiver}]")
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
                return
            }
            def label = crmInvitation.receiver
            crmInvitationService.cancel(crmInvitation.id)
            flash.warning = message(code: "crmInvitation.deleted.message", default: "Invitation to {0} deleted", args: [label])
        } else {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'crmInvitation.label', default: 'Invitation'), id])
        }
        redirect(action: "permissions")
    }

    def share(Long id, String email, String msg, String role) {
        def crmTenant = CrmTenant.get(id)
        if (!crmTenant) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        if (email) {
            def currentUser = crmSecurityService.currentUser
            if (email.trim().equalsIgnoreCase(currentUser?.email)) {
                flash.error = message(code: "crmInvitation.invite.self.message", default: "You cannot invite yourself", args: [crmTenant.name, email])
            } else {
                def alreadyInvited = crmSecurityService.getTenantPermissions(id).find{it.user.email.equalsIgnoreCase(email)}
                if (alreadyInvited) {
                    flash.error = message(code: "crmInvitation.invite.user.message", default: "User [{1}] already have access to {0}", args: [crmTenant.name, email])
                } else {
                    if (crmInvitationService.getInvitationsTo(email, id)) {
                        flash.error = message(code: "crmInvitation.invite.twice.message", default: "User [{0}] is already invited", args: [crmTenant.name, email])
                    } else {
                        event(for: "crm", topic: "tenantShared", data: [id: id, email: email, role: role, message: msg, user: crmSecurityService.currentUser.username])
                        flash.success = message(code: 'crmTenant.share.success.message', args: [crmTenant.name, email, msg])
                    }
                }
            }
        }

        if (params.referer) {
            redirect(uri: params.referer - request.contextPath)
        } else {
            redirect action: "permissions"
        }
    }
}
