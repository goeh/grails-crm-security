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

import grails.events.Listener
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.plugins.crm.core.CrmException
import grails.plugins.crm.core.CrmSecurityDelegate
import grails.plugins.crm.core.DateUtils
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.Pair
import grails.plugins.crm.util.Graph
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod
import org.codehaus.groovy.grails.web.util.WebUtils
import org.grails.plugin.platform.events.EventMessage

import javax.servlet.http.HttpServletRequest

/**
 * Generic security features, not security provider specific.
 */
class CrmSecurityService {

    static transactional = true

    def grailsApplication
    def crmFeatureService

    CrmSecurityDelegate crmSecurityDelegate

    /**
     * Checks if the current user is authenticated in this session.
     *
     * @return
     */
    boolean isAuthenticated() {
        crmSecurityDelegate.isAuthenticated()
    }

    /**
     * Checks if the current user has permission to perform an operation.
     *
     * @param permission wildcard permission
     * @return
     */
    boolean isPermitted(permission) {
        crmSecurityDelegate.isPermitted(permission.toString())
    }

    /**
     * Execute a piece of code as a specific user.
     *
     * @param username username
     * @param closure the work to perform
     * @return whatever the closure returns
     */
    def runAs(String username, Closure closure) {
        def user = CrmUser.findByUsernameAndStatus(username, CrmUser.STATUS_ACTIVE, [cache: true])
        if (!user) {
            throw new IllegalArgumentException("[$username] is not a valid user")
        }
        crmSecurityDelegate.runAs(username, closure)
    }

    /**
     * Execute a piece of code as a specific user in a specific tenant
     *
     * @param username username
     * @param tenant tenant id
     * @param closure the work to perform
     * @return whatever the closure returns
     */
    def runAs(String username, Long tenant, Closure closure) {
        def user = CrmUser.findByUsernameAndStatus(username, CrmUser.STATUS_ACTIVE, [cache: true])
        if (!user) {
            throw new IllegalArgumentException("[$username] is not a valid user")
        }
        crmSecurityDelegate.runAs(username) {
            TenantUtils.withTenant(tenant, closure)
        }
    }

    private CrmUser getEnabledUser(String username) {
        CrmUser.findByUsernameAndStatus(username, CrmUser.STATUS_ACTIVE, [cache: true])
    }

    /**
     * Create a new user.
     *
     * @param properties user domain properties.
     * @return the created CrmUser instance
     */
    CrmUser createUser(Map<String, Object> props) {
        if (CrmUser.findByUsername(props.username, [cache: true])) {
            throw new CrmException("user.exists.message", [props.username])
        }
        def user = new CrmUser()
        def args = [user, props, [include: CrmUser.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(user, 'bind', args.toArray())

        // Virtual property 'enabled' can set user status to STATUS_ACTIVE.
        if ((props.status == null) && props.enabled) {
            user.status = CrmUser.STATUS_ACTIVE
        }

        props.options.each { key, value ->
            user.setOption(key, value)
        }

        user.save(failOnError: true, flush: true)

        crmSecurityDelegate.createUser(user.username, props.password)

        event(for: "crm", topic: "userCreated", data: user.dao + [ip: props.ip])

        return user
    }

    /**
     * Update an existing user.
     *
     * @param username username
     * @param properties key/value pairs to update
     * @return the updated CrmUser instance
     */
    CrmUser updateUser(String username, Map<String, Object> props) {
        if (!username) {
            username = crmSecurityDelegate.currentUser
            if (!username) {
                throw new IllegalArgumentException("Can't update user [$username] because user is not authenticated")
            }
        }
        def user = CrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("[$username] is not a valid user")
        }

        def args = [user, props, [include: CrmUser.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(user, 'bind', args.toArray())

        user.save(failOnError: true, flush: true)

        if (props.password) {
            crmSecurityDelegate.setPassword(user.username, props.password)
        }

        // Use Spring Events plugin to broadcast that a user was updated.
        event(for: "crm", topic: "userUpdated", data: user.dao)

        return user
    }

    /**
     * Get the current user information.
     *
     * @return CrmUser instance for the current user, or null if not authenticated
     */
    CrmUser getCurrentUser() {
        getUser(null)
    }

    /**
     * Get user information for a user.
     *
     * @return a Map with user properties (username, name, email, ...)
     */
    Map<String, Object> getUserInfo(String username) {
        CrmUser.findByUsername(username, [cache: true])?.dao
    }

    /**
     * Delete a user.
     *
     * @param username username
     * @return true if the user was deleted
     */
    boolean deleteUser(String username) {
        def user = CrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("[$username] is not a valid user")
        }
        def accounts = CrmAccount.findAllByUser(user)
        for (a in accounts) {
            if (a.tenants) {
                throw new IllegalArgumentException("A user [${user.username}] with active accounts [${a}] cannot be deleted")
            }
        }
        // Accounts without tenants are ok to delete.
        for (a in accounts) {
            deleteAccount(a.id)
        }

        def userInfo = user.dao
        user.delete(flush: true)
        crmSecurityDelegate.deleteUser(username)
        event(for: "crm", topic: "userDeleted", data: userInfo)
        return true
    }

    CrmAccount getCurrentAccount() {
        def crmUser = getCurrentUser()
        if (!crmUser) {
            throw new IllegalArgumentException("User is not authenticated")
        }
        getAccounts(crmUser.username)?.find { it } // Return first account found.
    }

    /**
     * Create a new account.
     * @param params
     * @param map with products to add to the account
     * @return the created CrmAccount instance
     */
    CrmAccount createAccount(Map<String, Object> params = [:], Object products = null) {
        def user = params.user
        if (!user) {
            def username = crmSecurityDelegate.getCurrentUser()
            if (!username) {
                throw new IllegalArgumentException("Can't create tenant because user is not authenticated")
            }
            user = getEnabledUser(username)
            if (!user) {
                throw new CrmException("user.not.found.message", [username])
            }
        }
        def account = new CrmAccount()
        def args = [account, params, [include: CrmAccount.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(account, 'bind', args.toArray())

        account.user = user

        def status = params.status
        if (status) {
            if (status instanceof Number) {
                account.status = status
            } else {
                account.setStatusText(status.toString())
            }
        }

        if (!account.name) {
            account.name = user.name
        }
        if (!account.email) {
            account.email = user.email
        }

        def expires = params.remove('expires')
        if (expires) {
            if (expires instanceof Date) {
                if (!(expires instanceof java.sql.Date)) {
                    expires = new java.sql.Date(expires.clearTime().time)
                }
            } else {
                expires = DateUtils.parseSqlDate(expires.toString())
            }
            account.expires = expires
        }

        params.options.each { key, value ->
            account.setOption(key, value)
        }

        if (products) {
            if (products instanceof Map) {
                products.each { p, n ->
                    account.addToItems(productId: p, quantity: n)
                }
            } else {
                for (p in products) {
                    account.addToItems(productId: p, quantity: 1)
                }
            }
        }

        account.save(failOnError: true, flush: true)

        event(for: "crm", topic: "accountCreated", data: account.dao)

        return account
    }

    boolean deleteAccount(Long id) {

        def crmAccount = CrmAccount.get(id)
        if (!crmAccount) {
            throw new CrmException('crmAccount.not.found.message', ['Account', id])
        }
        if (crmAccount.tenants) {
            throw new CrmException('crmAccount.not.empty.message', ['Account', id])
        }

        def accountInfo = crmAccount.dao

        crmAccount.delete(flush: true)

        // Use platform-core events to broadcast that the account was deleted.
        // Receivers should remove any data associated with the account.
        event(for: "crm", topic: "accountDeleted", data: accountInfo)

        return true
    }

    List<CrmAccount> getAccounts(String username = null) {
        if (!username) {
            username = crmSecurityDelegate.currentUser
            if (!username) {
                throw new IllegalArgumentException("Can't list accounts because user is not authenticated")
            }
        }
        CrmAccount.createCriteria().list([sort: 'name', order: 'asc']) {
            user {
                eq('username', username)
            }
            cache true
        }
    }

    /**
     * Create new tenant, owned by the current user.
     *
     * @param tenantName name of tenant
     * @param params optional parameters (owner, locale, etc.)
     * @return the created CrmTenant instance
     */
    CrmTenant createTenant(CrmAccount account, String tenantName, Map<String, Object> params = [:], Closure initializer = null) {
        if (!tenantName) {
            throw new IllegalArgumentException("Can't create tenant because tenantName is null")
        }
        def username = crmSecurityDelegate.currentUser
        if (!username) {
            throw new IllegalArgumentException("Can't create tenant [$tenantName] because user is not authenticated")
        }
        def user = getEnabledUser(username)
        if (!user) {
            throw new CrmException("user.not.found.message", [username])
        }
        if (account.user != user) {
            throw new CrmException('crmAccount.permission.denied', ['Account', account.name])
        }
        def existing = CrmTenant.findByAccountAndName(account, tenantName, true, [cache: true])
        if (existing) {
            throw new IllegalArgumentException("Account [$account] already contains a tenant named [$tenantName]")
        }
        def parent = params.remove('parent')
        if (parent) {
            if (parent instanceof Number) {
                parent = CrmTenant.load(parent)
            }
            if (!parent) {
                throw new IllegalArgumentException("Can't create tenant [$tenantName] because parent tenant [${params.parent}] does not exist")
            }
        }

        // Create new tenant.
        def tenant = new CrmTenant(account: account, name: tenantName, parent: parent, locale: params.remove('locale')?.toString())

        params.each { key, value ->
            tenant.setOption(key, value)
        }

        tenant.save(failOnError: true, flush: true)

        addSystemRole(user, 'admin', tenant.id)

        if (initializer != null) {
            initializer.call(tenant.id)
        }

        enableDefaultFeatures(tenant.id)

        event(for: "crm", topic: "tenantCreated", data: tenant.dao)
        return tenant
    }

    /**
     * Enable features that should be enabled by default (including required features).
     *
     * @param tenant
     */
    private void enableDefaultFeatures(Long tenant) {
        for (feature in crmFeatureService.getApplicationFeatures().findAll { it.enabled }) {
            crmFeatureService.enableFeature(feature.name, tenant)
        }
    }

    @Listener(namespace = "*", topic = "enableFeature")
    def enableFeature(EventMessage msg) {
        def event = msg.data // [feature: feature, tenant: tenant, role:role, expires:expires]
        def feature = crmFeatureService.getApplicationFeature(event.feature)
        if (feature) {
            def securityConfig = grailsApplication.config.crm.security
            def permissions = securityConfig[feature]?.permission ?: feature.permissions
            def role = event.role
            if (permissions && role) {
                permissions = [(role): permissions[role]]
            }
            if (permissions) {
                setupFeaturePermissions(feature.name, permissions, event.tenant)
            }
        }
    }

    void setupFeaturePermissions(String feature, Map<String, List> permissionMap, Long tenant = TenantUtils.tenant) {
        permissionMap.each { roleName, permissions ->
            def role = CrmRole.findByNameAndTenantId(roleName, tenant, [lock: true])
            if (!role) {
                role = new CrmRole(name: roleName, param: roleName, tenantId: tenant)
            }
            if (!(permissions instanceof List)) {
                permissions = [permissions]
            }

            def alias = "${feature}.$roleName".toString()
            addPermissionAlias(alias, permissions)
            addPermissionIfMissing(role, alias)
            if (roleName == 'admin') {
                addPermissionIfMissing(role, "crmTenant:*:$tenant".toString())
                addPermissionIfMissing(role, "crmFeature:*:$tenant".toString())
            }
            role.save(failOnError: true, flush: true)
        }
    }

    private boolean addPermissionIfMissing(def target, String permission) {
        if (!target.permissions?.contains(permission)) {
            target.addToPermissions(permission)
            log.debug("Permission [$permission] added to tenant [${target.tenantId}]")
            return true
        }
        return false
    }

    /**
     * Update tenant properties.
     *
     * @param tenantId id of tenant to update
     * @param properties key/value pairs to update
     * @return updated CrmTenant instance or null if update failed
     */
    CrmTenant updateTenant(Long tenantId, Map<String, Object> properties) {
        def crmTenant = CrmTenant.get(tenantId)
        if (!crmTenant) {
            throw new CrmException('tenant.not.found.message', ['Tenant', tenantId])
        }
        if (properties.name) {
            crmTenant.name = properties.name
        }
        def parent = properties.parent
        if (parent) {
            if (parent instanceof Number) {
                parent = CrmTenant.load(parent)
            }
            crmTenant.parent = parent
        }
        properties.options.each { key, value ->
            crmTenant.setOption(key, value)
        }
        crmTenant.save(flush: true)
    }

    /**
     * Get the current executing tenant.
     *
     * @return the current CrmTenant instance
     */
    CrmTenant getCurrentTenant() {
        def tenant = TenantUtils.tenant
        return tenant ? CrmTenant.get(tenant) : null
    }

    /**
     * Get tenant instance.
     * @param id tenant id or null for current tenant
     * @return a CrmTenant instance or null if not found
     */
    CrmTenant getTenant(Long id = TenantUtils.tenant) {
        CrmTenant.get(id)
    }

    /**
     * Get tenant information.
     * @param id tenant id or null for current tenant
     * @return a Map with tenant properties (id, name, type, ...)
     */
    Map<String, Object> getTenantInfo(Long id = TenantUtils.tenant) {
        CrmTenant.get(id)?.dao
    }

    /**
     * Get all tenants that a user has access to.
     *
     * @param username username
     * @return collection of CrmTenant instances
     */
    List<CrmTenant> getTenants(String username = null) {
        if (!username) {
            username = crmSecurityDelegate.currentUser
            if (!username) {
                throw new IllegalArgumentException("not authenticated")
            }
        }
        def result = []
        try {
            def tenants = getAllTenants(username)
            if (tenants) {
                result = CrmTenant.createCriteria().list() {
                    inList('id', tenants)
                }
            }
        } catch (Exception e) {
            log.error("Failed to get tenants for user [$username]", e)
        }
        return result
    }

    /**
     * Check if current user can access the specified tenant.
     * @param username username
     * @param tenantId the tenant ID to check
     * @return true if user has access to the tenant (by it's roles, permissions or ownership)
     */
    boolean isValidTenant(Long tenantId, String username = null) {
        if (!username) {
            username = crmSecurityDelegate.currentUser
            if (!username) {
                return false
            }
        }

        def user = getEnabledUser(username)
        if (!user) {
            log.warn "SECURITY: User [${username}] is authenticated but not enabled!"
            return false
        }

        def tenant = CrmTenant.get(tenantId)
        if (!tenant) {
            return false
        }

        def today = new java.sql.Date(new Date().clearTime().time)
        def account = tenant.account
        // Owned tenants
        if (account.user == user && (account.expires == null || account.expires > today)) {
            return true
        }

        // Role tenants
        if (CrmUserRole.createCriteria().count() {
            eq('user', user)
            or {
                isNull('expires')
                ge('expires', today)
            }
            role {
                eq('tenantId', tenantId)
            }
            cache true
        }) {
            return true
        }

        // Permission tenants
        if (CrmUserPermission.createCriteria().count() {
            eq('user', user)
            eq('tenantId', tenantId)
            or {
                isNull('expires')
                ge('expires', today)
            }
            cache true
        }) {
            return true
        }
        return false
    }

    /**
     * Delete a tenant and all information associated with the tenant.
     * Checks will be performed to see if someone uses this tenant.
     *
     * @param id tenant id
     * @return true if the tenant was deleted
     */
    boolean deleteTenant(Long id, boolean force = false) {

        // Get tenant.
        def crmTenant = CrmTenant.get(id)
        if (!crmTenant) {
            throw new CrmException('crmTenant.not.found.message', ['Tenant', id])
        }

        def crmAccount = crmTenant.account

        // Make sure the account is owned by current user.
        def currentUser = getCurrentUser()
        if (crmAccount.user != currentUser) {
            throw new CrmException('crmTenant.permission.denied', ['Tenant', crmTenant.name])
        }

        // Make sure it's not the active tenant being deleted.
        if (TenantUtils.tenant == crmTenant.id) {
            throw new CrmException('crmTenant.delete.current.message', ['Tenant', crmTenant.name])
        }

        if (!force) {
            // Make sure it's not the default tenant being deleted.
            if (currentUser.defaultTenant == crmTenant.id) {
                throw new CrmException('crmTenant.delete.start.message', ['Tenant', crmTenant.name])
            }

            // Make sure we don't delete a tenant that is in use by other users (via roles)
            def affectedRoles = CrmUserRole.createCriteria().list() {
                role {
                    eq('tenantId', id)
                }
                cache true
            }
            def otherPeopleAffected = affectedRoles.findAll { it.user.id != currentUser.id }.collect { it.user }
            if (otherPeopleAffected) {
                throw new CrmException('crmTenant.delete.others.message', ['Tenant', crmTenant.name, otherPeopleAffected.join(', ')])
            }

            // Make sure we don't delete a tenant that is in use by other users (via permissions)
            def affectedPermissions = CrmUserPermission.findAllByTenantId(id)
            otherPeopleAffected = affectedPermissions.findAll { it.user.id != currentUser.id }.collect { it.user }
            if (otherPeopleAffected) {
                throw new CrmException('crmTenant.delete.others.message', ['Tenant', crmTenant.name, otherPeopleAffected.join(', ')])
            }
        }

        // Now we are ready to delete!
        def tenantInfo = crmTenant.dao

        // Subscribers (CRM plugins) must tell us if they need to delete/cleanup data associated with this tenant.
        // They do so by returning a Map with namespace, topic and dependencies.
        // Example return values:
        // crm-agreement plugin returns [namespace: 'crmAgreement', topic: 'nuke', dependencies: ['crmContact', 'crmTask']]
        // crm-task plugin returns [namespace: 'crmTask', topic: 'cleanup', dependencies: ['crmContact']]
        // crm-contact plugin returns [namespace: 'crmContact', topic: 'tenantDeleted']
        // This will result in the following synchronized events being sent last in this method:
        // event(for: 'crmAgreement', topic: 'nuke', data: tenantInfo)
        // event(for: 'crmTask', topic: 'cleanup', data: tenantInfo)
        // event(for: 'crmContact', topic: 'tenantDeleted', data: tenantInfo)
        // dependencies are important to calculate cascade order.
        def participants = event(for: "crmTenant", topic: "requestDelete", data: tenantInfo).values
        log.debug "deleteTenant tx participants=$participants"
        def graph = new Graph()
        for (p in participants) {
            if (!(p.namespace || p.topic)) {
                throw new RuntimeException("Invalid deleteTenant participant specification: $p")
            }
            if (p.dependencies) {
                for (d in p.dependencies) {
                    graph.addEdge(p.namespace, d)
                }
            } else {
                graph.addVertex(p.namespace)
            }
        }
        graph.each { v ->
            log.debug "event deleteTenant --------------> $v"
            event(for: v.toString(), topic: "deleteTenant", data: tenantInfo, fork: false)
        }

        // People who had this tenant as default tenant will have their defaultTenant reset.
        for (u in CrmUser.findAllByDefaultTenant(id)) {
            u.defaultTenant = null
            u.save()
        }

        crmAccount.discard()
        crmAccount = CrmAccount.lock(crmAccount.id)

        int n = 0
        for (r in CrmUserRole.createCriteria().list() {
            role {
                eq('tenantId', id)
            }
        }) {
            def u = r.user
            u.removeFromRoles(r)
            u.save()
            n++
        }
        log.info "Deleted $n user roles in tenant $id"

        for (p in CrmUserPermission.findAllByTenantId(id)) {
            def u = p.user
            u.removeFromPermissions(p)
            u.save()
            n++
        }
        log.info "Deleted $n user permissions in tenant $id"

        CrmRole.findAllByTenantId(id)*.delete()

        // Now lets nuke the tenant!
        crmAccount.removeFromTenants(crmTenant)
        crmTenant.delete()

        crmAccount.save(flush: true)

        // Receivers should remove any data associated with the tenant.
        event(for: "crm", topic: "tenantDeleted", data: tenantInfo)

        return true
    }

    void alert(HttpServletRequest request, String topic, String message = null) {
        log.warn "SECURITY ALERT! $topic ${message ?: ''} [uri=${WebUtils.getForwardURI(request)}, ip=${request.remoteAddr}, tenant=${TenantUtils.tenant}, session=${request.session?.id}]"
        def user
        def tenant
        try {
            user = currentUser
        } catch (Exception e) {
            // Ignore.
        }
        try {
            tenant = currentTenant
        } catch (Exception e) {
            // Ignore.
        }
        event for: "security", topic: topic, data: [request: request, message: message, user: user, tenant: tenant]
    }

    /**
     * Return the user domain instance for the current user.
     *
     * @param username (optional) username or null for current user
     * @return CrmUser instance
     */
    CrmUser getUser(String username = null) {
        if (!username) {
            username = crmSecurityDelegate.currentUser
        }
        username ? CrmUser.findByUsername(username, [cache: true]) : null
    }

    /**
     * Check if the argument is the current user.
     * The argument can be a Long (id), String (username) or CrmUser instance.
     *
     * @param arg Long (id), String (username) or CrmUser instance.
     * @return true if the current user is the same as specified user parameter
     */
    boolean isCurrentUser(arg) {
        def username = crmSecurityDelegate.currentUser
        if (arg instanceof Number) {
            return CrmUser.get(arg)?.username == username
        } else if (arg instanceof CrmUser) {
            return arg.username == username
        }
        return arg.toString() == username
    }

    private Set<Long> getAllTenants(String username = null) {
        if (!username) {
            username = crmSecurityDelegate.currentUser
            if (!username) {
                throw new IllegalArgumentException("not authenticated")
            }
        }
        def today = new java.sql.Date(new Date().clearTime().time)
        def result = new HashSet<Long>()
        def user = getEnabledUser(username)
        if (user) {
            // Owned tenants
            def tmp = CrmTenant.createCriteria().list() {
                projections {
                    property('id')
                }
                account {
                    eq('user', user)
                    or {
                        isNull('expires')
                        ge('expires', today)
                    }
                }
                cache true
            }
            if (tmp) {
                result.addAll(tmp)
            }
            // Role tenants
            tmp = CrmUserRole.createCriteria().list() {
                projections {
                    role {
                        property('tenantId')
                    }
                }
                eq('user', user)
                or {
                    isNull('expires')
                    ge('expires', today)
                }
                cache true
            }
            if (tmp) {
                result.addAll(tmp)
            }
            // Permission tenants
            tmp = CrmUserPermission.createCriteria().list() {
                projections {
                    property('tenantId')
                }
                eq('user', user)
                or {
                    isNull('expires')
                    ge('expires', today)
                }
                cache true
            }
            if (tmp) {
                result.addAll(tmp)
            }
        }
        return result
    }

    /**
     * Set the default tenant (ID) for a user.
     * @param username username or null for current user
     * @param tenant tenant id or null for current tenant
     * @return user information after updating user
     */
    Map<String, Object> setDefaultTenant(String username = null, Long tenant = null) {
        def user
        if (username) {
            user = getUser(username)
        } else {
            user = getUser()
            username = user.username
        }
        if (!tenant) {
            tenant = TenantUtils.getTenant()
        }
        if (tenant) {
            // Check that the user has permission to access this tenant.
            def availableTenants = getAllTenants(username)
            if (!availableTenants.contains(tenant)) {
                throw new IllegalArgumentException("Can't set default tenant to [$tenant] because it's not a valid tenant for user [$username]")
            }
        }
        user.discard()
        user = CrmUser.lock(user.id)
        user.defaultTenant = tenant
        user.save(flush: true)

        return user.dao
    }

    /**
     * Add a named permission to the system.
     * @param name name of permission
     * @param permissions List of  Wildcard permission strings
     */
    @CacheEvict(value = 'permissions', key = '#name')
    void addPermissionAlias(String name, List<String> permissions) {
        def perm = CrmNamedPermission.findByName(name)
        if (!perm) {
            perm = new CrmNamedPermission(name: name)
        }
        for (p in permissions) {
            if (!perm.permissions?.contains(p)) {
                perm.addToPermissions(p)
            }
        }
        perm.save(failOnError: true, flush: true)
    }

    @Cacheable('permissions')
    List<String> getPermissionAlias(String name) {
        CrmNamedPermission.findByName(name, [cache: true])?.permissions?.toList() ?: []
    }

    @CacheEvict(value = 'permissions', key = '#name')
    boolean removePermissionAlias(String name) {
        def perm = CrmNamedPermission.findByName(name)
        if (perm) {
            perm.delete()
            return true
        }
        return false
    }

    /**
     * Create a new role in the current tenant with a set of named permissions.
     * @param rolename name of role
     * @param permissions list of permission names (CrmNamedPermission.name)
     * @return the created CrmRole
     */
    CrmRole createRole(String rolename, List<String> permissions = []) {
        def tenant = TenantUtils.getTenant()
        def role = CrmRole.findByNameAndTenantId(rolename, tenant, [cache: true])
        if (role) {
            throw new IllegalArgumentException("Can't create role [$rolename] in tenant [$tenant] because role already exists")
        }
        role = new CrmRole(name: rolename, param: rolename, tenantId: tenant)
        for (perm in permissions) {
            role.addToPermissions(perm)
        }
        role.save(failOnError: true, flush: true)
    }

    boolean hasRole(String rolename, Long tenant = TenantUtils.tenant) {
        CrmRole.findByNameAndTenantId(rolename, tenant, [cache: true]) != null
    }

    CrmUserRole addUserRole(String username, String rolename, Date expires = null, Long tenant = null) {
        if (!tenant) {
            tenant = TenantUtils.getTenant()
        }
        def user = getEnabledUser(username)
        if (!user) {
            throw new IllegalArgumentException("Can't add role [$rolename] in tenant [$tenant] to user [$username] because user is not found")
        }
        def role = CrmRole.findByNameAndTenantId(rolename, tenant, [cache: true])
        if (!role) {
            role = new CrmRole(tenantId: tenant, name: rolename, param: rolename).save(failOnError: true, flush: true)
        }

        def (count, max) = getRoleUsage(rolename, tenant)
        if (max != null && count >= max) {
            throw new CrmException(rolename + '.role.max.exceeded', ['Tenant', count, max])
        }

        def userrole = CrmUserRole.findByUserAndRole(user, role, [cache: true])
        if (!userrole) {
            def expiryDate = expires != null ? new java.sql.Date(expires.clearTime().time) : null
            user.discard()
            user = CrmUser.lock(user.id)
            user.addToRoles(userrole = new CrmUserRole(role: role, expires: expiryDate))
            user.save(flush: true)
        }
        return userrole
    }

    private void addSystemRole(CrmUser user, String roleName, Long tenantId) {
        def role = CrmRole.findByNameAndTenantId(roleName, tenantId, [cache: true])
        if (!role) {
            role = new CrmRole(name: roleName, param: roleName, tenantId: tenantId).save(failOnError: true, flush: true)
        }
        new CrmUserRole(user: user, role: role).save(failOnError: true, flush: true)
    }

    List<Integer> getRoleUsage(String roleName, Long tenantId = null) {
        if (!tenantId) {
            tenantId = TenantUtils.tenant
        }
        def role = CrmRole.findByNameAndTenantId(roleName, tenantId, [cache: true])
        if (!role) {
            log.warn("Role [$roleName] not found in tenant [$tenantId]")
            return [0, 0]
        }
        def tenant = CrmTenant.get(role.tenantId)
        def account = tenant.account
        def count = CrmUserRole.countByRole(role)
        def maxProperty = 'crm' + role.name[0].toUpperCase() + role.name.substring(1)
        return [count, account.getItem(maxProperty)?.quantity ?: 0]
    }

    void addPermissionToRole(String permission, String rolename, Long tenant = TenantUtils.getTenant()) {
        def role = CrmRole.findByNameAndTenantId(rolename, tenant, [lock: true])
        if (!role) {
            throw new IllegalArgumentException("Can't add permission [$permission] to role [$rolename] in tenant [$tenant] because role is not found in this tenant")
        }
        if (!role.permissions?.contains(permission)) {
            role.addToPermissions(permission)
        }
    }

    void addPermissionToUser(String permission, String username = null, Long tenant = null, Date expires = null) {
        if (!tenant) {
            tenant = TenantUtils.getTenant()
        }
        if (!username) {
            username = crmSecurityDelegate.currentUser
            if (!username) {
                throw new IllegalArgumentException("not authenticated")
            }
        }
        def user = getEnabledUser(username)
        if (!user) {
            throw new IllegalArgumentException("Can't add permission [$permission] to user [$username] because user is not found")
        }
        def perm = CrmUserPermission.createCriteria().get() {
            eq('user', user)
            eq('tenantId', tenant)
            eq('permissionsString', permission)
            cache true
        }
        if (!perm) {
            user.discard()
            user = CrmUser.lock(user.id)
            user.addToPermissions(perm = new CrmUserPermission(tenantId: tenant, expires: expires, permissionsString: permission))
            user.save(flush: true)
        }
    }

    /**
     * Return a list of all permissions within a tenant.
     * @param tenant
     * @return list of CrmUserPermission and CrmUserRole instances
     */
    List getTenantPermissions(Long tenant = TenantUtils.getTenant()) {
        def result = []
        def roles = CrmUserRole.createCriteria().list() {
            role {
                eq('tenantId', tenant)
            }
            order 'id', 'asc'
            cache true
        }
        if (roles) {
            result.addAll(roles)
        }

        def permissions = CrmUserPermission.createCriteria().list() {
            eq('tenantId', tenant)
            order 'id', 'asc'
            cache true
        }
        if (permissions) {
            result.addAll(permissions)
        }

        return result
    }

    List<Map<String, Object>> getTenantUsers(Long tenant = TenantUtils.tenant) {
        getTenantPermissions(tenant).collect { it.user }.unique { it.username }.sort { it.username }.collect {
            [id: it.id, guid: it.guid, username: it.username, name: it.name, email: it.email]
        }
    }

    CrmRole updatePermissionsForRole(Long tenant = null, String rolename, List<String> permissions) {
        if (tenant == null) {
            tenant = TenantUtils.getTenant()
        }
        def role = CrmRole.findByNameAndTenantId(rolename, tenant, [lock: true])
        if (role) {
            role.permissions.clear()
        } else {
            role = new CrmRole(name: rolename, param: rolename, tenantId: tenant)
            log.warn("Created missing role [$rolename] for tenant [$tenant]")
        }
        for (perm in permissions) {
            role.addToPermissions(perm)
        }
        role.save(failOnError: true, flush: true)
    }

    void resetPermissions(Long tenantId = null) {
        if (tenantId == null) {
            tenantId = TenantUtils.getTenant()
        }
        def crmTenant = CrmTenant.get(tenantId)
        if (!crmTenant) {
            throw new CrmException('tenant.not.found.message', ['Tenant', tenantId])
        }

        for (feature in crmFeatureService.getFeatures(tenantId)) {
            def securityConfig = grailsApplication.config.crm.security
            def permissions = securityConfig[feature]?.permission ?: feature.permissions
            if (permissions) {
                setupFeaturePermissions(feature.name, permissions, tenantId)
            }
        }

        event(for: "crm", topic: "resetPermissions", data: [tenant: tenantId])
    }

    Pair hashPassword(String password) {
        def salt = crmSecurityDelegate.generateSalt()
        def hash = crmSecurityDelegate.hashPassword(password, salt)
        return new Pair<String, String>(hash, salt.encodeBase64().toString())
    }
}
