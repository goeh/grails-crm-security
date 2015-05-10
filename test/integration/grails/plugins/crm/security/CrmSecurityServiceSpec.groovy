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

class CrmSecurityServiceSpec extends grails.test.spock.IntegrationSpec {

    def crmSecurityService
    def crmFeatureService
    def crmThemeService
    def crmAccountService

    def "runAs changes current user"() {
        def result

        given:
        def user = crmSecurityService.createUser([username: "test8", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])

        when:
        crmSecurityService.runAs(user.username) {
            result = crmSecurityService.getCurrentUser()
        }
        then:
        result != null
        result.username == user.username
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
        def user = crmSecurityService.createUser([username: "test12", name: "Test User", email: "test@test.com", password: "test123"])

        when:
        crmSecurityService.runAs(user.username) {
            result = crmSecurityService.getCurrentUser()
        }
        then:
        thrown(Exception)
    }

    def "permission alias"() {
        when:
        crmSecurityService.addPermissionAlias "test", ['test:show', 'test:list']
        crmSecurityService.addPermissionAlias "foo", ['foo:*']
        crmSecurityService.addPermissionAlias "bar", ['bar:*']

        then:
        crmSecurityService.getPermissionAlias("foo") == ['foo:*']
        crmSecurityService.getPermissionAlias("bar") == ['bar:*']
        crmSecurityService.getPermissionAlias("foo") == ['foo:*']
        crmSecurityService.getPermissionAlias("bar") == ['bar:*']
        crmSecurityService.removePermissionAlias "foo"
        crmSecurityService.removePermissionAlias "bar"
        !crmSecurityService.getPermissionAlias("foo")
        !crmSecurityService.getPermissionAlias("bar")
    }

    def "theme feature"() {
        // Add a feature that should only be available under a specific theme.
        given:
        crmFeatureService.addApplicationFeatures {
            vanilla {
                description "A feature available for all tenant"
                link controller: "vanilla", action: "index"
                permissions {
                    read "vanilla:index,list,show"
                    update "vanilla:index,list:show,create,update"
                    admin "vanilla:*"
                }
                enabled true
            }
        }
        crmFeatureService.addApplicationFeatures {
            sunny {
                description "A feature only available in tenants with the 'sunny' theme"
                link controller: "sunny", action: "index"
                permissions {
                    read "sunny:index,list,show"
                    update "sunny:index,list:show,create,update"
                    admin "sunny:*"
                }
                theme "sunny"
                enabled true
            }
        }

        when:
        def user = crmSecurityService.createUser([username: "test13", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        def tenant1 = crmSecurityService.runAs(user.username) {
            def account = crmAccountService.createAccount(name: "Theme Account", status: CrmAccount.STATUS_ACTIVE)
            crmSecurityService.createTenant(account, "The sunny tenant", [theme: 'sunny'])
        }

        then:
        !crmThemeService.hasTheme('vanilla', tenant1.id)
        crmThemeService.hasTheme('sunny', tenant1.id)

        when:
        def permissions = CrmRole.findAllByTenantId(tenant1.id).collect { it.permissions }.flatten()

        then:
        permissions.contains('vanilla.admin')
        permissions.contains('vanilla.update')
        permissions.contains('vanilla.read')
        permissions.contains('sunny.admin')
        permissions.contains('sunny.admin')
        permissions.contains('sunny.admin')

        when:
        def tenant2 = crmSecurityService.runAs(user.username) {
            def account = crmAccountService.createAccount(name: "Vanilla Account", status: CrmAccount.STATUS_ACTIVE)
            crmSecurityService.createTenant(account, "The vanilla tenant")
        }

        then:
        !crmThemeService.hasTheme('vanilla', tenant2.id)
        !crmThemeService.hasTheme('sunny', tenant2.id)

        when:
        permissions = CrmRole.findAllByTenantId(tenant2.id).collect { it.permissions }.flatten()

        then:
        permissions.contains('vanilla.admin')
        permissions.contains('vanilla.update')
        permissions.contains('vanilla.read')
        !permissions.contains('sunny.admin')
        !permissions.contains('sunny.admin')
        !permissions.contains('sunny.admin')
    }
}
