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

import grails.plugins.crm.core.AuditEntity

/**
 * This domain class represents a CRM account.
 * An account holds customer and billing information.
 * An account can have many tenants, and one tenant can only be owned by one account.
 */
@AuditEntity
class CrmAccount {

    public static final Integer STATUS_BLOCKED = -1
    public static final Integer STATUS_CLOSED = 0
    public static final Integer STATUS_FREE = 1
    public static final Integer STATUS_TRIAL = 2
    public static final Integer STATUS_INVOICE = 3
    public static final Integer STATUS_ACTIVE = 4

    //String number
    String name
    String email
    String telephone
    String address1
    String address2
    String address3
    String postalCode
    String city
    String region
    String countryCode
    String ssn
    String reference

    CrmUser user

    java.sql.Date expires
    Integer status = STATUS_CLOSED

    static hasMany = [items: CrmAccountItem, options: CrmAccountOption, tenants: CrmTenant]

    static constraints = {
        //number(maxSize: 20, blank: false, unique: true)
        name(size: 2..80, maxSize: 80, nullable: false, blank: false)
        email(maxSize: 80, blank: false, email: true)
        telephone(size: 4..20, maxSize: 20, nullable: true)
        address1(maxSize: 80, nullable: true)
        address2(maxSize: 80, nullable: true)
        address3(maxSize: 80, nullable: true)
        postalCode(size: 2..20, maxSize: 20, nullable: true)
        city(size: 2..40, maxSize: 40, nullable: true)
        region(maxSize: 40, nullable: true)
        countryCode(size: 2..3, maxSize: 3, nullable: true)
        ssn(maxSize: 40, nullable: true)
        reference(maxSize: 40, nullable: true)
        user()
        expires(nullable: true)
        status(inList: [STATUS_BLOCKED, STATUS_CLOSED, STATUS_FREE, STATUS_TRIAL, STATUS_INVOICE, STATUS_ACTIVE])
    }

    static transients = ['dao', 'address', 'trial', 'active', 'closed', 'statusText']

    static mapping = {
        table 'crm_account'
        sort 'name'
        cache 'nonstrict-read-write'
        items sort: 'productId'
    }

    static namedQueries = {
        activeAccounts {
            gt('status', STATUS_CLOSED)
        }
    }

    static final List BIND_WHITELIST = [
            'name',
            'email',
            'telephone',
            'address1',
            'address2',
            'address3',
            'postalCode',
            'city',
            'region',
            'countryCode',
            'ssn',
            'reference'
    ]

    transient boolean isTrial() {
        status == STATUS_TRIAL
    }

    transient boolean isActive() {
        if (isClosed()) {
            return false
        }
        if (expires == null) {
            return true
        }
        def today = new java.sql.Date(new Date().clearTime().time)
        return expires > today
    }

    transient boolean isClosed() {
        status == STATUS_BLOCKED || status == STATUS_CLOSED
    }

    transient void setStatusText(String text) {
        switch (text?.trim()?.toLowerCase()) {
            case "active": status = STATUS_ACTIVE; break
            case "trial": status = STATUS_TRIAL; break
            case "closed": status = STATUS_CLOSED; break
            case "blocked": status = STATUS_BLOCKED; break
            case "free": status = STATUS_FREE; break
            case "invoice": status = STATUS_INVOICE; break
        }
    }

    transient String getStatusText() {
        switch (status) {
            case STATUS_ACTIVE: return "active"
            case STATUS_TRIAL: return "trial"
            case STATUS_CLOSED: return "closed"
            case STATUS_BLOCKED: return "blocked"
            case STATUS_FREE: return "free"
            case STATUS_INVOICE: return "invoice"
            default: return null
        }
    }

    transient Map<String, Object> getDao() {
        def map = [id: id, expires: expires]
        for (p in BIND_WHITELIST) {
            def v = this[p]
            if (v != null) {
                map[p] = v
            }
        }
        if (user) {
            map.user = [username: user.username]
        }
        if (tenants) {
            map.tenants = tenants.collect { [id: it.id, name: it.name] }
        }
        map.options = getOptionsMap()
        return map
    }

    boolean contains(String productId) {
        getItem(productId) != null
    }

    CrmAccountItem getItem(String productId) {
        items?.find { it.productId == productId }
    }

    void setItem(String productId, Integer quantity = 1, String unit = 'st') {
        def i = getItem(productId)
        if (i) {
            i.quantity = quantity
        } else {
            i = new CrmAccountItem(account: this, productId: productId, quantity: quantity, unit: unit)
            if (i.validate()) {
                addToItems(i)
            } else {
                log.error("Cannot add item [$productId] to account [$id] due to: ${i.errors.allErrors}")
            }
        }
    }

    int addItem(String productId, Integer quantity = 1, String unit = 'st') {
        def i = getItem(productId)
        if (i) {
            i.quantity = i.quantity + quantity
        } else {
            i = new CrmAccountItem(account: this, productId: productId, quantity: quantity, unit: unit)
            if (i.validate()) {
                addToItems(i)
            } else {
                log.error("Cannot add item [$productId] to account [$id] due to: ${i.errors.allErrors}")
            }
        }
    }

    /**
     * Return tenant parameters (options) as a Map.
     *
     * @return options
     */
    private Map<String, Object> getOptionsMap() {
        options.inject([:]) { map, o ->
            map[o.key] = o.value
            map
        }
    }

    void setOption(String key, Object value) {
        if (value == null) {
            removeOption(key)
        } else {
            def o = options.find { it.key == key }
            if (o != null) {
                o.value = value
            } else {
                addToOptions(new CrmAccountOption(key, value))
            }
        }
    }

    def getOption(String key = null) {
        if (key == null) {
            return getOptionsMap()
        }
        def o = options.find { it.key == key }
        return o != null ? o.value : null
    }

    boolean hasOption(String key) {
        getOption(key)
    }

    boolean removeOption(String key) {
        def o = options.find { it.key == key }
        if (o != null) {
            removeFromOptions(o)
            o.delete()
            return true
        }
        return false
    }

    String getAddress(boolean includePostalCode = true, String delimiter = ', ') {
        StringBuilder s = new StringBuilder()
        if (address1) {
            s << address1
        }
        if (address2) {
            if (s.length() > 0) {
                s << delimiter
            }
            s << address2
        }
        if (address3) {
            if (s.length() > 0) {
                s << delimiter
            }
            s << address3
        }
        if (postalCode && includePostalCode) {
            if (s.length() > 0) {
                s << delimiter
            }
            s << postalCode
        }
        if (city) {
            if (postalCode && includePostalCode) {
                s << ' '
            } else if (s.length() > 0) {
                s << delimiter
            }
            s << city
        }
        if (region) {
            if (s.length() > 0) {
                s << delimiter
            }
            s << region
        }
        if (countryCode) {
            if (s.length() > 0) {
                s << delimiter
            }
            s << countryCode
        }
        return s.toString()
    }

    String toString() {
        name.toString()
    }
}
