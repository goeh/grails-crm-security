/*
 * Copyright (c) 2012 Goran Ehrsson.
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

import grails.plugins.crm.feature.Feature

/**
 * Account Management.
 */
class CrmAccountService {

    def crmSecurityService
    def crmFeatureService

    List<Feature> getAccountFeatures(CrmAccount crmAccount) {
        def result = []
        for (p in crmAccount.items*.productId) {
            def f = crmFeatureService.getApplicationFeature(p)
            if (f) {
                result << f
            }
        }
        return result
    }

    boolean hasItem(CrmAccount account, String productId, Integer quantity = null) {
        def item = account.items?.find { it.productId == productId }
        if (item) {
            return quantity ? item.quantity >= quantity : true
        }
        return false
    }

    CrmAccountItem addItem(CrmAccount account, String productId, Integer quantity = null) {
        if (quantity == null) {
            quantity = 1
        }
        def item = new CrmAccountItem(account: account, productId: productId, quantity: quantity)
        if (item.validate()) {
            account.addToItems(item)
        }
        return item
    }

    Float getQuantity(CrmAccount account, String productId) {
        CrmAccountItem.createCriteria().get() {
            projections {
                property('quantity')
            }
            eq('account', account)
            eq('productId', productId)
            cache true
        } ?: 0f
    }

    Map getRoleStatistics(CrmAccount crmAccount) {
        def statistics = [:]
        for (tenant in crmAccount.tenants*.ident()) {
            def result = CrmUserRole.createCriteria().list() {
                role {
                    eq('tenantId', tenant)
                }
                cache true
            }
            for(userrole in result) {
                statistics.get(userrole.role.name, [] as Set) << userrole.user.username
            }
        }
        return statistics
    }
}
