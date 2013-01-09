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

import grails.plugins.crm.security.ResetPasswordDelegate

class CrmSecurityGrailsPlugin {
    // Dependency group
    def groupId = "grails.crm"
    // the plugin version
    def version = "1.0.3-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // Load after crm-core
    def loadAfter = ['crmCore']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "src/groovy/grails/plugins/crm/security/TestSecurityDelegate.groovy"
    ]

    def title = "Basic Security Features for Grails CRM"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''
This plugin provides basic security feature for Grails CRM.
Specific security implementeations exists for Apache Shiro (crm-security-shiro).
'''

    def documentation = "https://github.com/goeh/grails-crm-security"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-security/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-security"]

    def features = {
        security {
            description "Security Framework"
            link controller: 'crmTenant'
            enabled true
            required true
            hidden true
            permissions {
                guest "crmTenant:index", "crmSettings:*"
                user "crmTenant:index,create", "crmSettings:*"
                admin "crmSettings:*", "crmAccount:*"
                // NOTE Tenant specific permissions are added to each role
                // by CrmSecurityService#setupFeaturePermissions()
                // TODO is it possible to extend setupFeaturePermissions()
                // with support for this syntax? "crmTenant:activate:$tenant"
            }
        }
        register {
            description "User Registration"
            link controller: 'crmRegister'
            enabled true
            hidden true
        }
    }

    def doWithSpring = {
        // Register resetPasswordDelegate bean to be used by the reset-password plugin.
        resetPasswordDelegate(ResetPasswordDelegate) {bean ->
            bean.autowire = "byName"
        }
    }
}
