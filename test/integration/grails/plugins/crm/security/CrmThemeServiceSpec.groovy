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
 * Created by goran on 2014-01-20.
 */
class CrmThemeServiceSpec extends grails.plugin.spock.IntegrationSpec {

    def grailsApplication
    def crmSecurityService
    def crmAccountService
    def crmThemeService

    def "get theme name"() {
        when:
        def user = crmSecurityService.createUser([username: "themetester", name: "Theme Tester", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        CrmTenant tenant = crmSecurityService.runAs(user.username) {
            def account = crmAccountService.createAccount(name: "Theme Account", expires: new Date() + 30, status: CrmAccount.STATUS_TRIAL,
                    telephone: "+46800000", address1: "Box 123", postalCode: "12345", city: "Capital", reference: "test")
            crmSecurityService.createTenant(account, "Theme Tenant")
        }

        then:
        crmThemeService.getThemeName(tenant.id) == null

        when:
        grailsApplication.config.crm.theme.name = 'foo'

        then:
        crmThemeService.getThemeName(tenant.id) == 'foo'

        when:
        tenant.account.setOption('theme.name', 'bar')

        then:
        crmThemeService.getThemeName(tenant.id) == 'bar'

        when:
        tenant.setOption('theme.name', 'baz')

        then:
        crmThemeService.getThemeName(tenant.id) == 'baz'


        when:
        tenant.setOption('theme.name', null)

        then:
        crmThemeService.getThemeName(tenant.id) == 'bar'

        when:
        tenant.account.setOption('theme.name', null)

        then:
        crmThemeService.getThemeName(tenant.id) == 'foo'

    }

    def "get custom email sender"() {
        when:
        def user = crmSecurityService.createUser([username: "themetester", name: "Theme Tester", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        CrmTenant tenant = crmSecurityService.runAs(user.username) {
            def account = crmAccountService.createAccount(name: "Theme Account", expires: new Date() + 30, status: CrmAccount.STATUS_TRIAL,
                    telephone: "+46800000", address1: "Box 123", postalCode: "12345", city: "Capital", reference: "test")
            crmSecurityService.createTenant(account, "Theme Tenant")
        }

        then:
        crmThemeService.getEmailSender(tenant.id) == null

        when:
        grailsApplication.config.grails.mail.default.from = 'info@test.com'

        then:
        crmThemeService.getEmailSender(tenant.id) == 'info@test.com'

        when:
        tenant.account.setOption('mail.from', 'account@test.com')

        then:
        crmThemeService.getEmailSender(tenant.id) == 'Theme Account <account@test.com>'

        when:
        tenant.account.setOption('mail.from', 'My Account <account@test.com>')

        then:
        crmThemeService.getEmailSender(tenant.id) == 'My Account <account@test.com>'

        when:
        tenant.setOption('mail.from', 'tenant@test.com')

        then:
        crmThemeService.getEmailSender(tenant.id) == 'Theme Tenant <tenant@test.com>'

        when:
        tenant.setOption('mail.from', 'My Tenant <tenant@test.com>')

        then:
        crmThemeService.getEmailSender(tenant.id) == 'My Tenant <tenant@test.com>'
    }
}
