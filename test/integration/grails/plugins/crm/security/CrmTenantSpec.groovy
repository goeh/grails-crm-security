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

/**
 * Test ShiroCrmTenant and ShiroCrmTenantOption.
 */
class CrmTenantSpec extends grails.test.spock.IntegrationSpec {

    def crmSecurityService
    def crmAccountService

    def "create tenant"() {
        given:
        def result
        def user = crmSecurityService.createUser([username: "test5", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        def account = crmSecurityService.runAs(user.username) {
            crmAccountService.createAccount(name: "My Account", expires: new Date() + 30, status: CrmAccount.STATUS_TRIAL,
                    telephone: "+46800000", address1: "Box 123", postalCode: "12345", city: "Capital", reference: "test")
        }
        when:
        result = crmSecurityService.runAs(user.username) {
            crmSecurityService.getTenants()
        }
        then:
        result.isEmpty()

        when:
        result = crmSecurityService.runAs(user.username) {
            crmSecurityService.createTenant(account, "My First Tenant")
            crmSecurityService.createTenant(account, "My Second Tenant")
            crmSecurityService.getTenants()
        }
        then:
        result.size() == 2
    }

    def "create tenant with locale"() {

        given:
        def result = []
        def swedish = new Locale("sv", "SE")
        def spanish = new Locale("es", "ES")
        def user = crmSecurityService.createUser([username: "test16", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        def account = crmSecurityService.runAs(user.username) { crmAccountService.createAccount(status: CrmAccount.STATUS_ACTIVE) }

        when:
        crmSecurityService.runAs(user.username) {
            result << crmSecurityService.createTenant(account, "Default")
            result << crmSecurityService.createTenant(account, "Svenska", [locale: swedish])
            result << crmSecurityService.createTenant(account, "Español", [locale: spanish])
        }
        then:
        result[0].locale == null
        result[1].locale == swedish.toString()
        result[2].locale == spanish.toString()
    }

    def "update tenant"() {
        def tenant

        given:
        def user = crmSecurityService.createUser([username: "test6", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        def account = crmSecurityService.runAs(user.username) { crmAccountService.createAccount(status: CrmAccount.STATUS_ACTIVE) }

        when:
        crmSecurityService.runAs(user.username) { tenant = crmSecurityService.createTenant(account, "My Tenant") }
        then:
        crmSecurityService.getTenantInfo(tenant.id)?.name == "My Tenant"

        when:
        crmSecurityService.updateTenant(tenant.id, [name: "Our Tenant"])

        then:
        crmSecurityService.getTenantInfo(tenant.id)?.name == "Our Tenant"
    }

    def "set tenant options"() {
        def tenant

        given:
        def user = crmSecurityService.createUser([username: "test7", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        def account = crmSecurityService.runAs(user.username) { crmAccountService.createAccount(status: CrmAccount.STATUS_ACTIVE) }

        when:
        crmSecurityService.runAs(user.username) {
            tenant = crmSecurityService.createTenant(account, "My Tenant")
        }
        then:
        tenant.getOption('foo') == null

        when:
        tenant = crmSecurityService.updateTenant(tenant.id, [options: [foo: 42]])

        then:
        tenant.getOption('foo') == 42
        crmSecurityService.getTenantInfo(tenant.id).options.foo == 42
    }

    def "set and get tenant options"() {
        given:
        def user = crmSecurityService.createUser(username: "inttest", name: "Integration Test", email: "test@test.com", password: "secret", status: CrmUser.STATUS_ACTIVE)
        def t

        crmSecurityService.runAs(user.username) {
            def account = crmAccountService.createAccount(status: CrmAccount.STATUS_ACTIVE)
            t = crmSecurityService.createTenant(account, "test")
        }

        when:
        t.setOption("foo", 42)
        t.setOption("bar", 43)

        then:
        t.getOption("foo") == 42
        t.getOption("bar") == 43
        t.dao.options.foo == 42
        t.dao.options.bar == 43

        when:
        t.removeOption("bar")

        then:
        t.getOption("foo") == 42
        t.dao.options.foo == 42
        t.getOption("bar") == null
        t.dao.options.bar == null
        t.hasOption("foo")
        !t.hasOption("bar")
    }
}
