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

import grails.plugins.crm.core.TenantUtils
import grails.validation.Validateable

/**
 * Command object used in the user settings page.
 */
@Validateable
class CrmSettingsEditCommand {
    String username
    String name
    String company
    String email
    String telephone
    String postalCode
    String timezone
    Long defaultTenant
    String startPage

    def crmSecurityService
    def crmFeatureService

    static constraints = {
        importFrom CrmUser, include: ['name', 'company', 'email', 'telephone', 'postalCode']
        username(size: 2..80, maxSize: 80, nullable: false, blank: false)
        timezone(maxSize: 40, nullable: true)
        defaultTenant(nullable: true, validator: { val, obj ->
            def rval = null
            if (val) {
                CrmTenant.withNewSession {
                    if (!obj.crmSecurityService.isValidTenant(val)) {
                        rval = ['crmUser.defaultTenant.invalid.message', 'defaultTenant', 'User', val]
                    }
                }
            }
            return rval
        })
        startPage(maxSize: 80, nullable: true, validator: { val, obj ->
            def rval = null
            if (val && obj.defaultTenant) {
                CrmTenant.withNewSession {
                    TenantUtils.withTenant(obj.defaultTenant) {
                        if (!obj.crmSecurityService.isPermitted(val)) {
                            rval = ['crmUser.startPage.invalid.message', 'startPage', 'User', val]
                        }
                    }
                }
            }
            return rval
        })
    }
}
