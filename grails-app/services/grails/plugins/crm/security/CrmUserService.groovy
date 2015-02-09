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
import grails.plugins.crm.core.SearchUtils

/**
 * User administration service.
 */
class CrmUserService {

    static transactional = true
    static selectable = 'list'

    def crmThemeService
    def crmSecurityService

    /**
     * Empty query = search all records.
     *
     * @param params pagination parameters
     * @return List of CrmUser domain instances
     */
    def list(Map params) {
        list([:], params)
    }

    /**
     * Find CrmUser instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List of CrmUser domain instances
     */
    def list(Map query, Map params) {

        final Set<Long> users = [] as Set
        if (query.theme) {
            final List<CrmTenant> result = crmThemeService.findAllTenantsByTheme(query.theme)
            for (t in result) {
                for (Map u in crmSecurityService.getTenantUsers(t.id)) {
                    users << u.id
                }
            }
            if (users.isEmpty()) {
                users << 0L
            }
        }

        CrmUser.createCriteria().list(params) {
            if (query.username) {
                ilike('username', SearchUtils.wildcard(query.username))
            }
            if (query.name) {
                ilike('name', SearchUtils.wildcard(query.name))
            }
            if (query.company) {
                ilike('company', SearchUtils.wildcard(query.company))
            }
            if (query.email) {
                ilike('email', SearchUtils.wildcard(query.email))
            }
            if (query.status) {
                eq('status', Integer.valueOf(query.status))
            }
            if (users) {
                inList('id', users)
            }
        }
    }

    CrmUser findUserByEmail(String email, boolean allowDuplicates = false) {
        def result = CrmUser.findAllByEmail(email, [cache: true])
        if (result.size() == 0) {
            return null
        } else if ((result.size() == 1) || allowDuplicates) {
            return result.find { it }
        }
        throw new IllegalStateException("Found ${result.size()} users with email [$email]")
    }

    List<CrmTheme> getThemesForUser(final CrmUser user) {
        Set<CrmTheme> themes = [] as Set
        for (t in crmSecurityService.getTenants(user.username)) {
            def theme = crmThemeService.getTheme(t.id)
            if (theme) {
                themes << theme
            }
        }
        return themes.toList()
    }
}
