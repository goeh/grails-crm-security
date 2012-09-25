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
class CrmPermissionController {

    static allowedMethods = [list: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 70,
                    title: 'crmNamedPermission.index.label',
                    action: 'list'
            ]
    ]

    def grailsApplication

    def list() {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def result = CrmNamedPermission.list(params)
        [result: result, totalCount: result.totalCount]
    }

    def create() {
        switch (request.method) {
            case 'GET':
                return [crmNamedPermission: new CrmNamedPermission(params)]
            case 'POST':
                def crmNamedPermission = new CrmNamedPermission()
                def permissions = params.list('permissions').findAll {it}
                def data = [name: params.name, permissions: permissions]
                bindData(crmNamedPermission, data)
                if (!crmNamedPermission.save(flush: true)) {
                    render view: 'create', model: [crmNamedPermission: crmNamedPermission]
                    return
                }

                flash.success = message(code: 'crmNamedPermission.created.message', args: [message(code: 'crmNamedPermission.label', default: 'Permission'), crmNamedPermission.toString()])
                redirect action: 'list'
                break
        }
    }

    def edit() {
        def crmNamedPermission = CrmNamedPermission.get(params.id)
        if (!crmNamedPermission) {
            flash.error = message(code: 'crmNamedPermission.not.found.message', args: [message(code: 'crmNamedPermission.label', default: 'Permission'), params.id])
            redirect action: 'list'
            return
        }

        switch (request.method) {
            case 'GET':
                return [crmNamedPermission: crmNamedPermission]
            case 'POST':
                if (params.version && crmNamedPermission.version) {
                    def version = params.version.toLong()
                    if (crmNamedPermission.version > version) {
                        crmNamedPermission.errors.rejectValue('version', 'crmNamedPermission.optimistic.locking.failure',
                                [message(code: 'crmNamedPermission.label', default: 'User')] as Object[],
                                "Another user has updated this user while you were editing")
                        render view: 'edit', model: [crmNamedPermission: crmNamedPermission]
                        return
                    }
                }
                def permissions = params.list('permissions').findAll {it}
                def data = [name: params.name, permissions: permissions]
                bindData(crmNamedPermission, data)

                if (!crmNamedPermission.save(flush: true)) {
                    render view: 'edit', model: [crmNamedPermission: crmNamedPermission]
                    return
                }

                flash.success = message(code: 'crmNamedPermission.updated.message', args: [message(code: 'crmNamedPermission.label', default: 'Permission'), crmNamedPermission.toString()])
                redirect action: 'list', id: crmNamedPermission.id
                break
        }
    }

    def delete() {
        def crmNamedPermission = CrmNamedPermission.get(params.id)
        if (!crmNamedPermission) {
            flash.error = message(code: 'crmNamedPermission.not.found.message', args: [message(code: 'crmNamedPermission.label', default: 'Permission'), params.id])
            redirect action: 'list'
            return
        }

        try {
            def tombstone = crmNamedPermission.toString()
            crmNamedPermission.delete(flush: true)
            flash.warning = message(code: 'crmNamedPermission.deleted.message', args: [message(code: 'crmNamedPermission.label', default: 'Permission'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmNamedPermission.not.deleted.message', args: [message(code: 'crmNamedPermission.label', default: 'Permission'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

}
