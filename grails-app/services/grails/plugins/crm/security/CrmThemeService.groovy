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

import grails.plugins.crm.core.CrmTheme
import grails.plugins.crm.core.TenantUtils
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

/**
 * A service for getting and settings theme parameters.
 */
class CrmThemeService {

    public static final String CACHE_NAME = "crmtheme"
    public static final String OPTION_THEME_NAME = "theme.name"
    private static final String OPTION_EMAIL_FROM = "mail.from"

    def grailsApplication

    CacheManager grailsCacheManager

    private static final Map<String, Long> themeTenants = [:]

    void registerThemeTenant(String themeName, Long tenant) {
        if (tenant != null) {
            themeTenants[themeName] = tenant
            log.debug "Tenant [$tenant] is now registered as manager for theme [$themeName]"
        } else {
            themeTenants.remove(themeName)
            log.debug "Theme [$themeName] is no longer managed by a tenant"
        }
    }

    Long getTenantForTheme(String themeName) {
        Long t = themeTenants[themeName]

        if (t != null) {
            return t
        }

        t = CrmTenantOption.createCriteria().get() {
            projections {
                property('tenant.id')
            }
            eq('key', OPTION_THEME_NAME)
            eq('v', """{"v":"${themeName}"}""") // TODO This is a hack that knows how JSON is stored, please fix!
            maxResults 1
        }

        if (t == null) {
            t = grailsApplication.config.crm.theme.tenant."$themeName" ?: null
        }

        if (t != null) {
            themeTenants[themeName] = t
        }

        return t
    }

    /**
     * Return the tenant that is managing the specified tenant's theme.
     *
     * @param tenant the current tenant
     * @return the tenant ID of a "theme tenant"
     */
    Long getThemeTenant(Long tenant = null) {
        if (tenant == null) {
            tenant = TenantUtils.tenant
        }
        String theme = getThemeName(tenant)
        return theme ? getTenantForTheme(theme) : null
    }

    String getThemeForAccount(Long account) {
        def crmAccount = CrmAccount.get(account)
        if (!crmAccount) {
            throw new IllegalArgumentException("No such account: $account")
        }
        crmAccount.getOption(OPTION_THEME_NAME)
    }

    void setThemeForAccount(Long account, String themeName) {
        def crmAccount = CrmAccount.get(account)
        if (!crmAccount) {
            throw new IllegalArgumentException("No such account: $account")
        }
        crmAccount.setOption(OPTION_THEME_NAME, themeName)
        log.debug "Account #$account [$crmAccount.name] is now using theme [$themeName]"
    }

    void setThemeForTenant(Long tenant, String themeName) {
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        crmTenant.setOption(OPTION_THEME_NAME, themeName)
        log.debug "Tenant #$tenant [$crmTenant.name] is now using theme [$themeName]"
    }

    /**
     * Return a CrmTheme instance for a tenant
     * @param id tenant id to lookup the theme for
     * @return a CrmTheme instance or null if no theme was found for the specified tenant
     */
    CrmTheme getTheme(Long id) {
        final Cache cache = grailsCacheManager.getCache(CACHE_NAME)
        CrmTheme theme = cache.get(id)?.get()
        if (theme == null) {
            final String themeName = getThemeName(id)
            theme = themeName ? new CrmTheme(themeName, getTenantForTheme(themeName) ?: 1L) : null
            cache.put(id, theme ?: false)
        }
        return theme ?: null
    }

    String getThemeName(Long tenant) {
        if (tenant) {
            def crmTenant = CrmTenant.get(tenant)
            if (!crmTenant) {
                log.warn("No such tenant: $tenant")
                return null
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
        }
        def config = grailsApplication.config.crm.theme.name
        return config ? config.toString() : null
    }

    boolean hasTheme(String name, Long tenant = null) {
        if(tenant == null) {
            tenant = TenantUtils.tenant
        }
        getThemeName(tenant) == name
    }

    void setEmailSenderForAccount(Long account, String email) {
        def crmAccount = CrmAccount.get(account)
        if (!crmAccount) {
            throw new IllegalArgumentException("No such account: $account")
        }
        crmAccount.setOption(OPTION_EMAIL_FROM, email)
        log.debug "Account #$account [$crmAccount.name] is now using [$email] as email sender"
    }

    void setEmailSenderForTenant(Long tenant, String email) {
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        crmTenant.setOption(OPTION_EMAIL_FROM, email)
        log.debug "Tenant #$tenant [$crmTenant.name] is now using [$email] as email sender"
    }

    String getEmailSender(Long tenant, String configKey = null) {
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            log.warn("No such tenant: $tenant")
            return null
        }
        def opt = crmTenant.getOption(OPTION_EMAIL_FROM)
        if (opt) {
            def addr = opt.toString()
            if (addr.contains('<')) {
                return addr
            }
            return "${crmTenant.name} <${addr}>".toString()
        }

        def crmAccount = crmTenant.account
        opt = crmAccount.getOption(OPTION_EMAIL_FROM)
        if (opt) {
            def addr = opt.toString()
            if (addr.contains('<')) {
                return addr
            }
            return "${crmAccount.name} <${addr}>".toString()
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
        if (size == null) {
            throw new IllegalArgumentException("logo size cannot be null")
        }
        def crmAccount = CrmAccount.get(account)
        if (!crmAccount) {
            throw new IllegalArgumentException("No such account: $account")
        }
        crmAccount.setOption('logo.' + size, path)
        log.debug "Account #$account [$crmAccount.name] is now using [$path] as $size logo"
    }

    void setLogoForTenant(Long tenant, String size, String path) {
        if (size == null) {
            throw new IllegalArgumentException("logo size cannot be null")
        }
        def crmTenant = CrmTenant.get(tenant)
        if (!crmTenant) {
            throw new IllegalArgumentException("No such tenant: $tenant")
        }
        crmTenant.setOption('logo.' + size, path)
        log.debug "Tenant #$tenant [$crmTenant.name] is now using [$path] as $size logo"
    }

    String getLogo(Long tenant, String size = 'medium') {
        def path
        if (tenant) {
            def crmTenant = CrmTenant.get(tenant)
            if (!crmTenant) {
                log.warn("No such tenant: $tenant")
                return null
            }
            path = crmTenant.getOption('logo.' + size)
            if (!path) {
                def crmAccount = crmTenant.account
                path = crmAccount.getOption('logo.' + size)
            }
        }
        if (!path) {
            path = grailsApplication.config.crm.theme.logo."$size"
        }
        path ?: null
    }

    File getLogoFile(Long tenant, String size = 'medium') {
        String path = getLogo(tenant, size)
        if (path) {
            return grailsApplication.mainContext.getResource(path)?.getFile()
        }
        return null
    }

    List<CrmTenant> findAllTenantsByTheme(String themeName) {

        def id = CrmAccountOption.createCriteria().get() {
            projections {
                property('account.id')
            }
            eq('key', OPTION_THEME_NAME)
            eq('v', """{"v":"${themeName}"}""") // TODO This is a hack that knows how JSON is stored, please fix!
            maxResults 1
        }
        if(!id) {
            return [].asImmutable()
        }

        CrmTenant.createCriteria().list() {
            account {
                eq('id', id)
            }
        }
    }
}
