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
import grails.plugins.crm.core.TenantUtils

/**
 * Theme related UI tags.
 * @since 2.0.4.
 */
class CrmThemeTagLib {

    public static String namespace = "crm"

    def crmThemeService

    def theme = { attrs ->
        final CrmTheme theme = crmThemeService.getTheme(TenantUtils.tenant)
        if (theme) {
            out << theme[attrs.property ?: 'name']
        }
    }

    def hasTheme = { attrs, body ->
        def themeName = attrs.theme
        if (!themeName) {
            throwTagError("Tag [hasTheme] is missing required attribute [theme]")
        }

        if (crmThemeService.hasTheme(themeName)) {
            out << body()
        }
    }
}
