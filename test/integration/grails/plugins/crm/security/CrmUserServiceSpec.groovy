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
 * Created by goran on 2014-10-29.
 */
class CrmUserServiceSpec extends grails.plugin.spock.IntegrationSpec {

    def grailsApplication
    def crmSecurityService
    def crmAccountService
    def crmThemeService
    def crmUserService
    def grailsEventsRegistry

    def "list all users with access to a specific theme"() {
        given:
        def themes = [green: 5, yellow: 2, red: 1]
        grailsEventsRegistry.on("crmTenant", "getUsers") { event ->
            def result = CrmUserRole.createCriteria().list() {
                projections {
                    user {
                        property('id')
                    }
                }
                role {
                    eq('tenantId', event.tenant)
                }
            }.collect {
                [id: it]
            }
            return result
        }

        when: "Create three accounts, five green tenants, two yellow tenant and one red tenant"
        for (theme in themes) {
            CrmUser admin = crmSecurityService.createUser([username: theme.key, name: "$theme.key user", email: "$theme.key@test.com", password: theme.key, status: CrmUser.STATUS_ACTIVE])
            CrmAccount account = crmAccountService.createAccount([user     : admin, name: "$theme.key account", expires: new Date() + 30, status: CrmAccount.STATUS_ACTIVE,
                                                                  telephone: "+46800000", address1: "Box 123", postalCode: "12345", city: "Capital", reference: theme.key,
                                                                  options  : [(CrmThemeService.OPTION_THEME_NAME): theme.key]], [crmUser: 8])
            theme.value.times { n ->
                CrmTenant t = crmSecurityService.runAs(admin.username) {
                    crmSecurityService.createTenant(account, "$theme.key #$n")
                }
                CrmUser user = crmSecurityService.createUser([username: "u$n$theme.key", name: "u$n$theme.key user", email: "u$n$theme.key@test.com", password: theme.key, status: CrmUser.STATUS_ACTIVE])
                crmSecurityService.addUserRole(user, 'user', null, t.id)
            }
        }

        then:
        crmThemeService.findAllTenantsByTheme('green').size() == themes.green
        crmThemeService.findAllTenantsByTheme('yellow').size() == themes.yellow
        crmThemeService.findAllTenantsByTheme('red').size() == themes.red

        crmUserService.list([theme: 'green'], [:]).size() == themes.green + 1
        crmUserService.list([theme: 'yellow'], [:]).size() == themes.yellow + 1
        crmUserService.list([theme: 'red'], [:]).size() == themes.red + 1

        crmUserService.getThemesForUser(crmSecurityService.getUser('green')) == ['green']
        crmUserService.getThemesForUser(crmSecurityService.getUser('yellow')) == ['yellow']
        crmUserService.getThemesForUser(crmSecurityService.getUser('red')) == ['red']
    }
}
