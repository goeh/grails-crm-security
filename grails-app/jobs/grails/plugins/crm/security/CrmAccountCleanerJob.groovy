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

/**
 * Closed expired accounts.
 */
class CrmAccountCleanerJob {
    static triggers = {
        cron name: 'crmAccountCleaner', cronExpression: "0 3 0 * * ?" // every day 3 minutes after midnight.
    }

    def group = 'crm-account'

    def grailsApplication

    def execute() {
        if (grailsApplication.config.crm.security.job.accountcleaner.enabled) {
            def result = CrmAccount.createCriteria().list() {
                not {
                    eq 'status', CrmAccount.STATUS_CLOSED
                }
                lt 'expires', new java.sql.Date(new Date().clearTime().time)
            }
            if (result) {
                log.debug "Closing ${result.size()} expired accounts"
                for (account in result) {
                    account.status = CrmAccount.STATUS_CLOSED
                    account.save()
                    log.info "Closing account #${account.id} <${account.user.email}> \"${account.name}\" expired ${account.expires}"
                }
            }
        }
    }
}
