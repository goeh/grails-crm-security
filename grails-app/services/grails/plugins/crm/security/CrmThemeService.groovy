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
 * A service for getting and settings theme parameters.
 */
class CrmThemeService {

    private static final String OPTION_THEME_NAME = "theme.name"
    private static final String OPTION_EMAIL_FROM = "mail.from"

    def grailsApplication
    def crmSecurityService

    void setThemeForAccount(Long account, String themeName) {
        def crmAccount = CrmAccount.get(account)
        if (!crmAccount) {
            throw new IllegalArgumentException("No such account: $account")
        }
        crmAccount.setOption(OPTION_THEME_NAME, themeName)
    }

    void setThemeForTenant(Long tenant, String themeName) {
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        crmTenant.setOption(OPTION_THEME_NAME, themeName)
    }

    String getThemeName(Long tenant) {
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        def opt = crmTenant.getOption(OPTION_THEME_NAME)
        if (opt) {
            return opt.toString()
        }

        def crmAccount = crmTenant.account
        opt = crmAccount.getOption(OPTION_THEME_NAME)
        if (opt) {
            return opt.toString()
        }

        def config = grailsApplication.config.crm.theme.name
        return config ? config.toString() : null
    }

    void setEmailSenderForAccount(Long account, String email) {
        def crmAccount = CrmAccount.get(account)
        if (!crmAccount) {
            throw new IllegalArgumentException("No such account: $account")
        }
        crmAccount.setOption(OPTION_EMAIL_FROM, email)
    }

    void setEmailSenderForTenant(Long tenant, String email) {
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        crmTenant.setOption(OPTION_EMAIL_FROM, email)
    }

    String getEmailSender(Long tenant, String configKey = null) {
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        def opt = crmTenant.getOption(OPTION_EMAIL_FROM)
        if (opt) {
            def addr = opt.toString()
            if (addr.contains('<')) {
                return addr
            } else {
                return "${crmTenant.name} <${addr}>".toString()
            }
        }

        def crmAccount = crmTenant.account
        opt = crmAccount.getOption(OPTION_EMAIL_FROM)
        if (opt) {
            def addr = opt.toString()
            if (addr.contains('<')) {
                return addr
            } else {
                return "${crmAccount.name} <${addr}>".toString()
            }
        }

        def config
        if (configKey) {
            config = grailsApplication.config[configKey]
        }
        if (!config) {
            config = grailsApplication.config.grails.mail.default.from
        }
        return config ? config.toString() : null
    }

    void setLogoForAccount(Long account, String size, String path) {
        if(size == null) {
            throw new IllegalArgumentException("logo size cannot be null")
        }
        def crmAccount = CrmAccount.get(account)
        if (!crmAccount) {
            throw new IllegalArgumentException("No such account: $account")
        }
        crmAccount.setOption('logo.' + size, path)
    }

    void setLogoForTenant(Long tenant, String size, String path) {
        if(size == null) {
            throw new IllegalArgumentException("logo size cannot be null")
        }
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        crmTenant.setOption('logo.' + size, path)
    }

    String getLogo(Long tenant, String size = 'medium') {
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        def path = crmTenant.getOption('logo.' + size)
        if(!path) {
            def crmAccount = crmTenant.account
            path = crmAccount.getOption('logo.' + size)
            if (!path) {
                path = grailsApplication.config.crm.theme.logo."$size"
            }
        }
        path ?: null
    }

    File getLogoFile(Long tenant, String size = 'medium') {
        String path = getLogo(tenant, size)
        if(path) {
            return grailsApplication.mainContext.getResource(path)?.getFile()
        }
        return null
    }
}
