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

import grails.plugins.crm.core.TenantUtils

/**
 * Created by goran on 2014-01-20.
 */
class CrmThemeServiceSpec extends grails.plugin.spock.IntegrationSpec {

    def grailsApplication
    def crmSecurityService
    def crmAccountService
    def crmThemeService
    def grailsLinkGenerator // CrmThemeLinkGenerator configured in resources.groovy

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
        crmThemeService.setThemeForAccount(tenant.accountId, 'bar')

        then:
        crmThemeService.getThemeName(tenant.id) == 'bar'

        when:
        crmThemeService.setThemeForTenant(tenant.id, 'baz')

        then:
        crmThemeService.getThemeName(tenant.id) == 'baz'


        when:
        crmThemeService.setThemeForTenant(tenant.id, null)

        then:
        crmThemeService.getThemeName(tenant.id) == 'bar'

        when:
        crmThemeService.setThemeForAccount(tenant.accountId, null)

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
        crmThemeService.setEmailSenderForAccount(tenant.accountId, 'account@test.com')

        then:
        crmThemeService.getEmailSender(tenant.id) == 'Theme Account <account@test.com>'

        when:
        crmThemeService.setEmailSenderForAccount(tenant.accountId, 'My Account <account@test.com>')

        then:
        crmThemeService.getEmailSender(tenant.id) == 'My Account <account@test.com>'

        when:
        crmThemeService.setEmailSenderForTenant(tenant.id, 'tenant@test.com')

        then:
        crmThemeService.getEmailSender(tenant.id) == 'Theme Tenant <tenant@test.com>'

        when:
        crmThemeService.setEmailSenderForTenant(tenant.id, 'My Tenant <tenant@test.com>')

        then:
        crmThemeService.getEmailSender(tenant.id) == 'My Tenant <tenant@test.com>'
    }

    def "get custom logo"() {
        when:
        def user = crmSecurityService.createUser([username: "logotester", name: "Theme Logo Tester", email: "logo@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        CrmTenant tenant = crmSecurityService.runAs(user.username) {
            def account = crmAccountService.createAccount(name: "Logo Account", expires: new Date() + 30, status: CrmAccount.STATUS_TRIAL,
                    telephone: "+46800000", address1: "Box 123", postalCode: "12345", city: "Capital", reference: "test")
            crmSecurityService.createTenant(account, "Logo Tenant")
        }

        then:
        crmThemeService.getLogo(tenant.id) == null

        when:
        grailsApplication.config.crm.theme.logo.medium = '/images/logo-medium.png'

        then:
        crmThemeService.getLogo(tenant.id) == '/images/logo-medium.png'

        when:
        crmThemeService.setLogoForAccount(tenant.accountId, 'medium', '/images/logo-foo.png')

        then:
        crmThemeService.getLogo(tenant.id) == '/images/logo-foo.png'

        when:
        crmThemeService.setLogoForTenant(tenant.id, 'medium', '/images/logo-bar.png')

        then:
        crmThemeService.getLogo(tenant.id) == '/images/logo-bar.png'

        when:
        crmThemeService.setLogoForTenant(tenant.id, 'large', '/images/logo-bar-large.png')

        then:
        crmThemeService.getLogo(tenant.id, 'small') == null
        crmThemeService.getLogo(tenant.id) == '/images/logo-bar.png'
        crmThemeService.getLogo(tenant.id, 'large') == '/images/logo-bar-large.png'

        when:
        crmThemeService.setLogoForTenant(tenant.id, 'large', null)

        then:
        crmThemeService.getLogo(tenant.id) == '/images/logo-bar.png'

        when:
        crmThemeService.setLogoForTenant(tenant.id, 'medium', null)

        then:
        crmThemeService.getLogo(tenant.id) == '/images/logo-foo.png'

        when:
        crmThemeService.setLogoForAccount(tenant.accountId, 'medium', null)

        then:
        crmThemeService.getLogo(tenant.id) == '/images/logo-medium.png'
        crmThemeService.getLogoFile(tenant.id).name == 'logo-medium.png'
    }

    def "validate serverURL"() {
        given:
        def test1
        def test2
        def test3
        def test4
        def config = grailsApplication.config.crm.theme
        // Test 1
        //config.serverURL = "https://app.domain.se"
        //config.cookie.domain = 'app.domain.se'
        //config.cookie.path = '/'
        // Test 2
        config.test2.serverURL = "https://secure.domain.se"
        config.test2.cookie.domain = 'secure.domain.se'
        config.test2.cookie.path = '/'
        // Test 3
        config.test3.serverURL = "https://www.domain.se/secure"
        config.test3.cookie.domain = 'www.domain.se'
        config.test3.cookie.path = '/secure'
        // Test 4
        config.test4.cookie.domain = 'test.domain.se'
        config.test4.cookie.path = '/'

        when:
        def user = crmSecurityService.createUser([username: "test", password: "test",
                email: "test@test.com", name: "Test", enabled: true])
        crmSecurityService.runAs(user.username) {
            def account = crmAccountService.createAccount([status: "active"], [:])
            test1 = crmSecurityService.createTenant(account, "Test 1")
            test2 = crmSecurityService.createTenant(account, "Test 2")
            test2.setOption('theme.name', 'test2')
            test2.save()
            test3 = crmSecurityService.createTenant(account, "Test 3")
            test3.setOption('theme.name', 'test3')
            test3.save()
            test4 = crmSecurityService.createTenant(account, "Test 4")
            test4.setOption('theme.name', 'test4')
            test4.save()
            return account
        }

        then:
        TenantUtils.withTenant(test1.id) { grailsLinkGenerator.getServerBaseURL() } == 'http://www.domain.se'
        TenantUtils.withTenant(test2.id) { grailsLinkGenerator.getServerBaseURL() } == 'https://secure.domain.se'
        TenantUtils.withTenant(test3.id) { grailsLinkGenerator.getServerBaseURL() } == 'https://www.domain.se/secure'
        TenantUtils.withTenant(test4.id) { grailsLinkGenerator.getServerBaseURL() } == 'http://test.domain.se'
        grailsLinkGenerator.getServerBaseURL() == 'http://www.domain.se'

        when:
        config.serverURL = "https://app.domain.se"

        then:
        TenantUtils.withTenant(test1.id) { grailsLinkGenerator.getServerBaseURL() } == 'https://app.domain.se'
        grailsLinkGenerator.getServerBaseURL() == 'https://app.domain.se'
    }
}
