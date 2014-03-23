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


import grails.plugins.crm.security.ResetPasswordDelegate

class CrmSecurityGrailsPlugin {
    def groupId = "grails.crm"
    def version = "1.2.8.6"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def loadAfter = ['crmCore']
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "src/groovy/grails/plugins/crm/security/TestSecurityDelegate.groovy"
    ]

    def title = "Basic Security Features for GR8 CRM"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''
This plugin provides basic security feature for GR8 CRM.
Specific security implementations exists for Apache Shiro (crm-security-shiro).
'''
    def documentation = "https://github.com/goeh/grails-crm-security"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-security/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-security"]

    def doWithSpring = {
        // Register resetPasswordDelegate bean to be used by the reset-password plugin.
        resetPasswordDelegate(ResetPasswordDelegate) {bean ->
            bean.autowire = "byName"
        }
    }
}
