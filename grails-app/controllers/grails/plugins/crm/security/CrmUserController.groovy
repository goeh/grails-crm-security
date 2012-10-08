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

import org.springframework.dao.DataIntegrityViolationException

/**
 * User administration.
 */
class CrmUserController {

    static allowedMethods = [list: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 70,
                    title: 'crmUser.index.label',
                    action: 'index'
            ]
    ]

    def crmSecurityService
    def crmUserService

    def index() {
        def cmd = new CrmUserQueryCommand()
        bindData(cmd, params)
        [cmd: cmd]
    }

    def list() {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def result
        try {
            result = crmUserService.list(params, params)
            [result: result, totalCount: result.totalCount]
        } catch (Exception e) {
            flash.error = e.message
            [result: [], totalCount: 0]
        }
    }

    def show() {
        def crmUser = CrmUser.get(params.id)
        if (!crmUser) {
            flash.error = message(code: 'crmUser.not.found.message', args: [message(code: 'crmUser.label', default: 'User'), params.id])
            redirect action: 'index'
            return
        }
        def tenants = crmSecurityService.getTenants(crmUser.username)
        [crmUser: crmUser, tenantList: tenants]
    }

    def edit() {
        def crmUser = CrmUser.get(params.id)
        if (!crmUser) {
            flash.error = message(code: 'crmUser.not.found.message', args: [message(code: 'crmUser.label', default: 'User'), params.id])
            redirect action: 'index'
            return
        }
        def tenants = crmSecurityService.getTenants(crmUser.username)

        switch (request.method) {
            case 'GET':
                return [crmUser: crmUser, tenantList: tenants]
            case 'POST':
                if (params.version && crmUser.version) {
                    def version = params.version.toLong()
                    if (crmUser.version > version) {
                        crmUser.errors.rejectValue('version', 'crmUser.optimistic.locking.failure',
                                [message(code: 'crmUser.label', default: 'User')] as Object[],
                                "Another user has updated this user while you were editing")
                        render view: 'edit', model: [crmUser: crmUser, tenantList: tenants]
                        return
                    }
                }

                bindData(crmUser, params, [include: CrmUser.BIND_WHITELIST])

                if (!crmUser.save(flush: true)) {
                    render view: 'edit', model: [crmUser: crmUser, tenantList: tenants]
                    return
                }

                flash.success = message(code: 'crmUser.updated.message', args: [message(code: 'crmUser.label', default: 'User'), crmUser.toString()])
                redirect action: 'show', id: crmUser.id
                break
        }
    }

    def delete() {
        def crmUser = CrmUser.get(params.id)
        if (!crmUser) {
            flash.error = message(code: 'crmUser.not.found.message', args: [message(code: 'crmUser.label', default: 'User'), params.id])
            redirect action: 'index'
            return
        }

        if (crmUser.accounts) {
            flash.error = message(code: 'crmUser.delete.accounts.message', args: [message(code: 'crmUser.label', default: 'User'), crmUser.toString()])
            redirect action: 'edit', id: crmUser.id
            return
        }

        def tombstone = crmUser.toString()
        try {
            if (crmSecurityService.deleteUser(crmUser.username)) {
                flash.warning = message(code: 'crmUser.deleted.message', args: [message(code: 'crmUser.label', default: 'User'), tombstone])
                redirect action: 'index'
                return
            }
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete user [${crmUser.username}]", e)
        }
        flash.error = message(code: 'crmUser.not.deleted.message', args: [message(code: 'crmUser.label', default: 'User'), tombstone])
        redirect action: 'show', id: params.id
    }
}
