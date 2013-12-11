/*
 * Copyright (c) 2013 Goran Ehrsson.
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

/**
 * Remove entries from CrmTenantLog older than n days.
 */
class CrmTenantLogCleanerJob {
    static triggers = {
        cron name: 'crmTenantLogCleaner', cronExpression: "0 0 1 ? * 1" // Every Sunday at 1 am.
    }

    def group = 'crm-tenant'

    def grailsApplication
    def crmTenantLogService

    def execute() {
        if (grailsApplication.config.crm.security.job.logcleaner.enabled) {
            def offset = grailsApplication.config.crm.tenant.log.clean ?: 30
            def date = new Date() - offset
            def result = crmTenantLogService.list([to: date], [max: 1000])
            if (result.size()) {
                log.info "Deleting ${result.size()} tenant log events before ${date}"
                result*.delete()
            }
        }
    }
}
