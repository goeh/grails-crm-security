/*
 * Copyright (c) 2015 Goran Ehrsson.
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

import grails.plugins.crm.core.CrmTheme
import grails.test.mixin.TestFor

/**
 * Tests for the CrmThemeTagLib class.
 */
@TestFor(CrmThemeTagLib)
class CrmThemeTagLibTests {

    public static final String THEME_NAME = "test"

    void testThemeName() {
        def taglib = applicationContext.getBean(CrmThemeTagLib)
        taglib.crmThemeService = [
                getTheme: { new CrmTheme(THEME_NAME, 42L) },
                hasTheme: { name -> true }
        ]
        assert applyTemplate('<crm:theme/>') == THEME_NAME
        assert applyTemplate('<crm:theme property="name"/>') == THEME_NAME
        assert applyTemplate('<crm:theme property="tenant"/>') == "42"
    }
}
