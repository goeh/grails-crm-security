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

}
