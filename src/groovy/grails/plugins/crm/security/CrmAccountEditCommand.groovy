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

import grails.validation.Validateable

/**
 * Command object used when editing account information.
 */
@Validateable
class CrmAccountEditCommand implements Serializable {
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
    String plan

    static constraints = {
        //importFrom CrmAccount
        name(size: 2..80, maxSize: 80, blank: false)
        email(maxSize: 80, blank: false, email: true)
        telephone(size: 4..20, maxSize: 20, nullable: true)
        address1(maxSize: 80, nullable: true)
        address2(maxSize: 80, nullable: true)
        address3(maxSize: 80, nullable: true)
        postalCode(size: 2..20, maxSize: 20, blank: false)
        city(size: 2..40, maxSize: 40, blank: false)
        region(maxSize: 40, nullable: true)
        countryCode(size: 2..3, maxSize: 3, nullable: true)
        ssn(maxSize: 40, nullable: true)
        reference(maxSize: 40, nullable: true)
        plan(maxSize: 80, nullable: true)
    }

    String toString() {
        name.toString()
    }
}
