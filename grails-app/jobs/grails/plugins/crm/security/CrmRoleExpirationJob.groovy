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

import org.grails.plugin.platform.events.EventMessage

/**
 * Trigger application event [crmUserRole.expired] for each expired user role.
 * The event is sent once the day after role expired.
 */
class CrmRoleExpirationJob {
    static triggers = {
        cron name: 'crmRoleExpiration', cronExpression: "0 30 10 * * ?" // every day at 10.30 am
        //simple(name: 'crmRoleExpirationTest', startDelay: 1000 * 60 * 2, repeatInterval: 1000 * 60 * 2)
    }

    def group = 'crm-account'

    def grailsEventsPublisher

    def execute() {
        def result = CrmUserRole.createCriteria().list() {
            lt 'expires', new java.sql.Date(new Date().clearTime().time)
        }
        log.debug "Found ${result.size()} expired user roles"
        for (role in result) {
            grailsEventsPublisher.event(new EventMessage("expired",
                    [tenant: role.role.tenantId, user: role.user.username, id: role.id, role: role.role.param, expires: role.expires],
                    "crmUserRole", true))
        }
    }
}
