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
import org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator

/**
 * A CRM-theme-aware link generator.
 */
class CrmThemeLinkGenerator extends DefaultLinkGenerator {

    def grailsApplication
    def crmThemeService

    CrmThemeLinkGenerator(final String serverUrl) {
        super(serverUrl)
    }

    CrmThemeLinkGenerator(final String serverUrl, final String contextPath) {
        super(serverUrl, contextPath)
    }

    @Override
    String makeServerURL() {
        Long tenant = TenantUtils.tenant
        def url
        if (tenant) {
            def theme = crmThemeService.getThemeName(tenant)
            if (theme) {
                def config = grailsApplication.config.crm.theme."$theme"
                if (config) {
                    url = config.serverURL
                    if (!url) {
                        // Try to figure out serverURL from theme cookie configuration.
                        def domain = config.cookie.domain
                        if (domain) {
                            if (config.cookie.secure) {
                                url = "https://$domain"
                            } else {
                                url = "http://$domain"
                            }
                            def path = config.cookie.path
                            if (!path) {
                                path = contextPath
                            }
                            if (path && path != '/') {
                                url += path
                            }
                        }
                    }
                }
            }
        }

        if(! url) {
            url = grailsApplication.config.crm.theme.serverURL
            if(! url) {
               url = super.makeServerURL()
            }
        }

        return url
    }
}
