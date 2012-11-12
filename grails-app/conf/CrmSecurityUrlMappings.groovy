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

class CrmSecurityUrlMappings {

    static mappings = {
        // CrmRegisterController
        name 'crm-register': "/register" {
            controller = 'crmRegister'
            action = 'index'
        }
        // CrmSettingsController
        name 'crm-user-settings': "/settings" {
            controller = 'crmSettings'
            action = 'index'
        }
        // CrmAccountController
        name 'crm-account': "/account" {
            controller = 'crmAccount'
            action = 'index'
        }
        // CrmTenantController
        name 'crm-tenant': "/tenant" {
            controller = 'crmTenant'
            action = 'index'
        }
        name 'crm-tenant-create': "/tenant/create" {
            controller = 'crmTenant'
            action = 'create'
        }
        name 'crm-tenant-activate': "/tenant/activate/$id" {
            controller = 'crmTenant'
            action = 'activate'
        }
        name 'crm-permissions': "/tenant/permissions" {
            controller = 'crmTenant'
            action = 'permissions'
        }
    }
}