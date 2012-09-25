/*
*  Copyright 2012 Goran Ehrsson.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*  under the License.
*/
package grails.plugins.crm.security

class CrmSecurityServiceSpec extends grails.plugin.spock.IntegrationSpec {

    def crmSecurityService
    def grailsApplication
    def crmFeatureService

    def "create user"() {
        when:
        def user = crmSecurityService.createUser([username: "test1", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        then:
        user != null
        user instanceof Map
    }

    def "duplicate username is not allowed"() {
        when:
        def user = crmSecurityService.createUser([username: "test2", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        then:
        user != null

        when:
        user = crmSecurityService.createUser([username: "test2", name: "Test User Duplicate", email: "info@technipelago.se", password: "test789", enabled: true])
        then:
        thrown(Exception)
    }

    def "get user information"() {
        when:
        crmSecurityService.createUser([username: "test3", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        def info = crmSecurityService.getUserInfo("test3")

        then:
        info instanceof Map
        info.username == "test3"
        info.name == "Test User"
        info.email == "test@test.com"
        info.enabled
    }

    def "get user instance"() {
        when:
        crmSecurityService.createUser([username: "test4", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        def user = crmSecurityService.getUser("test4")

        then:
        user instanceof CrmUser
        user.username == "test4"
        user.name == "Test User"
        user.email == "test@test.com"
        user.enabled
    }

    def "create tenant"() {
        given:
        def result
        def features
        crmSecurityService.createUser([username: "test5", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test5") {
            result = crmSecurityService.getTenants()
        }
        then:
        result.isEmpty()

        when:
        crmSecurityService.runAs("test5") {
            def t1 = crmSecurityService.createTenant("My First Tenant")
            def t2 = crmSecurityService.createTenant("My Second Tenant")
            result = crmSecurityService.getTenants()
            features = crmFeatureService.getFeatures(t1.id)
        }
        then:
        result.size() == 2
        features.find {it.name == 'security'}
    }

    def "update tenant"() {
        def tenant

        given:
        crmSecurityService.createUser([username: "test6", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test6") {
            tenant = crmSecurityService.createTenant("My Tenant")
        }
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
        crmSecurityService.createUser([username: "test7", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test7") {
            tenant = crmSecurityService.createTenant("My Tenant")
        }
        then:
        tenant.options.foo == null

        when:
        tenant = crmSecurityService.updateTenant(tenant.id, [options: [foo: 42]])

        then:
        tenant.options.foo == 42
        crmSecurityService.getTenantInfo(tenant.id).options.foo == 42
    }

    def "runAs changes current user"() {
        def result

        given:
        crmSecurityService.createUser([username: "test8", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test8") {
            result = crmSecurityService.getCurrentUser()
        }
        then:
        result != null
        result.username == "test8"
    }

    def "runAs with non-existing username should throw exception"() {
        def result
        // No user created here.
        when:
        crmSecurityService.runAs("test11") {
            result = crmSecurityService.getCurrentUser()
        }
        then:
        thrown(Exception)
    }

    def "runAs with disabled user should throw exception"() {
        def result

        given:
        crmSecurityService.createUser([username: "test12", name: "Test User", email: "test@test.com", password: "test123", enabled: false])

        when:
        crmSecurityService.runAs("test12") {
            result = crmSecurityService.getCurrentUser()
        }
        then:
        thrown(Exception)
    }

    def "create tenant with locale"() {

        given:
        def result = []
        def locale = Locale.default
        def swedish = new Locale("sv", "SE")
        def spanish = new Locale("es", "ES")
        crmSecurityService.createUser([username: "test16", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test16") {
            result << crmSecurityService.createTenant("Default")
            result << crmSecurityService.createTenant("Svenska", [locale: swedish])
            result << crmSecurityService.createTenant("Español", [locale: spanish])
        }
        then:
        result[0].locale == locale
        result[1].locale == swedish
        result[2].locale == spanish
    }

    def "test default permissions"() {
        given:
        def securityConfig = grailsApplication.config.crm.security
        def tenant
        securityConfig.default.permission.guest = ["crmTenant:index,activate"]
        securityConfig.default.permission.user = ["crmTenant:index,activate,create,edit"]
        securityConfig.default.permission.admin = ["crmTenant:*"]
        crmSecurityService.createUser([username: "test17", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test17") {
            tenant = crmSecurityService.createTenant("Default")
        }

        then:
        tenant != null
        CrmRole.countByTenantId(tenant.id) == 3
    }
}
