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

import grails.validation.Validateable

/**
 * Command Object used in user registration.
 */
@Validateable
class RegisterUserCommand implements Serializable {

    String username
    String name
    String company
    String email
    String password
    String telephone
    String postalCode
    String country
    String campaign
    String captcha
    boolean accepted

    def grailsApplication

    static constraints = {
        importFrom CrmUser, include: ['name', 'company', 'email', 'telephone', 'postalCode', 'campaign']

        username(size: 2..80, maxSize: 80, nullable: false, blank: false, validator: { val, obj ->
            CrmUser.withNewSession {
                if (CrmUser.countByUsername(val)) {
                    return ['register.not.unique.message', 'username', 'User', val]
                }
            }
        })
        password(maxSize: 80, nullable: false, blank: false)
        country(size: 2..3, maxSize: 3, blank: false)
        captcha(maxSize: 10, blank: false)
        accepted(validator: { val, obj ->
            if (obj.grailsApplication.config.crm.register.legal && !val) {
                return 'register.legal.false.message'
            }
            return null
        })
    }

    Map<String, Object> toMap() {
        def map = ['username', 'name', 'company', 'email', 'telephone', 'password',
                'postalCode', 'campaign', 'captcha', 'accepted'].inject([:]) { m, p ->
            m[p] = this[p]
            return m
        }
        if (postalCode) {
            postalCode = postalCode.replaceAll(/\W/, '')
        }
        if (country) {
            // Country codes can be stored as 2- or 3-letter ISO3166 codes.
            def config = grailsApplication.config
            if ((config.crm.register.countryCode.length == 2 && country.length() == 3)
                    || (config.crm.register.countryCode.length == 3 && country.length() == 2)) {
                map.countryCode = convertISO3166(country)
            } else {
                map.countryCode = country
            }
        }

        return map
    }

    private static final Map countryCodes = Locale.getISOCountries().inject([:]) { map, twoLetterCode ->
        def l = new Locale("", twoLetterCode)
        map[l.getISO3Country()] = l.getCountry()
        return map
    }

    /**
     * Convert between ISO3166-alpha2 and ISO3166-alpha3 codes.
     * If argument is three letters, the two letter representation is returned.
     * If argument is two letters, the three letter representation is returned.
     *
     * @params countryCode two or three letter ISO3166 country code.
     */
    private String convertISO3166(String countryCode) {
        if (countryCode == null) {
            throw new IllegalArgumentException("countryCode is null")
        }
        switch (countryCode.length()) {
            case 2:
                return new Locale("", countryCode).getISO3Country()
            case 3:
                return countryCodes[countryCode]
            default:
                throw new IllegalArgumentException("Invalid length of country code: " + countryCode)
        }
    }
}
