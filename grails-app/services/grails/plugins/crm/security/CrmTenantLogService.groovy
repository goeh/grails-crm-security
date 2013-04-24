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

import grails.plugins.crm.core.CrmSecurityDelegate
import grails.plugins.crm.core.TenantUtils

/**
 * Service that helps you log events to CrmTenantLog.
 */
class CrmTenantLogService {

    CrmSecurityDelegate crmSecurityDelegate

    def log(String category, String message) {
        log(TenantUtils.tenant, new Date(), crmSecurityDelegate.currentUser, category, message)
    }

    def log(String username, String category, String message) {
        log(TenantUtils.tenant, new Date(), username, category, message)
    }

    def log(Long tenantId, Date timestamp, String username, String category, String message) {
        new CrmTenantLog(tenantId: tenantId, timestamp: timestamp, username: username, category: category, message: message).save()
    }

    List list(Map filter = [:], Map params = [:]) {
        CrmTenantLog.createCriteria().list(params) {
            if (filter.tenant != null || filter.tenantId != null) {
                eq('tenantId', Long.valueOf(filter.tenant != null ? filter.tenant : filter.tenantId))
            }
            if (filter.username || filter.user) {
                eq('username', filter.username ?: filter.user)
            }
            if (filter.category) {
                eq('category', filter.category)
            }
            if (filter.from && filter.to) {
                between('timestamp', filter.from, filter.to)
            } else if (filter.from) {
                ge('timestamp', filter.from)
            } else if (filter.to) {
                le('timestamp', filter.to)
            }
        }
    }
}
