# Grails CRM - Security Plugin

CRM = [Customer Relationship Management](http://en.wikipedia.org/wiki/Customer_relationship_management)

Grails CRM is a set of [Grails Web Application Framework](http://www.grails.org/)
plugins that makes it easy to develop web application with CRM functionality.
With CRM we mean features like:

- Contact Management
- Task/Todo Lists
- Project Management

## Common security features

This plugin contains common security features for Grails CRM.

- Multi-tenancy (built-in)
- User roles and permissions

Authentication is done by specific plugins for each security implementation.
For example crm-security-shiro that uses Apache Shiro for security.

## Related plugins
The [crm-security-ui](https://github.com/technipelago/grails-crm-security-ui) plugin provides
a Twitter Bootstrap based user interface for managing application security.

## Examples

    def user = crmSecurityService.createUser(username: "test", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE)

    crmSecurityService.runAs(user.username) {
        def account = crmAccountService.createAccount(name: "My Test Account", expires: new Date() + 30, status: CrmAccount.STATUS_TRIAL)

        def t1 = crmSecurityService.createTenant(account, "My First Tenant")
        def t2 = crmSecurityService.createTenant(account, "My Second Tenant")

        TenantUtils.withTenant(t1.id) {
            // Do work in "My First Tenant" as user "test"...
        }
    }