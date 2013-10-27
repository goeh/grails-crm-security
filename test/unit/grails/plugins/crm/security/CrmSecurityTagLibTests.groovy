/*
 * Copyright 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.security

import grails.test.mixin.TestFor

@TestFor(CrmSecurityTagLib)
class CrmSecurityTagLibTests {

    void testUserIsNotAuthenticated() {
        def taglib = applicationContext.getBean(CrmSecurityTagLib)
        taglib.crmSecurityService = [
                isAuthenticated: { false },
                getCurrentUser: { new CrmUser() },
                getUserInfo: { uname -> [username: uname, name: uname?.toUpperCase()]}
        ]
        // Make sure tag returns nothing since we are not logged in.
        assert applyTemplate("<crm:user>\${username}</crm:user>") == ""
    }

    void testUserIsAuthenticated() {
        def taglib = applicationContext.getBean(CrmSecurityTagLib)
        taglib.crmSecurityService = [
                isAuthenticated: { true },
                getCurrentUser: { new CrmUser(username: "test", name: "Test User") },
                getUserInfo: { uname -> [username: "test", name: "Test User"] }
        ]
        // Make sure tag returns the principal since we are logged in.
        assert applyTemplate("<crm:user>\${username}</crm:user>") == "test"
    }

    void testOtherUserInfo() {
        def taglib = applicationContext.getBean(CrmSecurityTagLib)
        taglib.crmSecurityService = [
                isAuthenticated: { true },
                getCurrentUser: { new CrmUser(username: "test", name: "Test User") },
                getUserInfo: { uname -> [username: uname, name: uname.toUpperCase()] }
        ]
        // Make sure tag returns the user info we specified.
        assert applyTemplate("<crm:user username=\"foo\">\${name}</crm:user>") == "FOO"
    }

    void testUserNotFound() {
        def taglib = applicationContext.getBean(CrmSecurityTagLib)
        taglib.crmSecurityService = [
                isAuthenticated: { true },
                getCurrentUser: { new CrmUser(username: "test", name: "Test User") },
                getUserInfo: { uname -> null }
        ]
        // Make sure tag returns the user info we specified.
        assert applyTemplate("<crm:user username=\"foo\">\${name}</crm:user>") == ""
        assert applyTemplate("<crm:user username=\"foo\" nouser=\"bar\">\${name}</crm:user>") == "bar"
    }
}
