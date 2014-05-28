package grails.plugins.crm.security

/**
 * Test spec for CrmTenantLogService.
 */
class CrmTenantLogSpec extends grails.test.spock.IntegrationSpec {

    def crmTenantLogService

    def "log a few events and try to list them"() {
        when:
        crmTenantLogService.log('test', 'Hello World')
        crmTenantLogService.log('me', 'test', 'Foo')
        crmTenantLogService.log(1L, new Date() - 1, 'you', 'test', 'Bar')

        then:
        crmTenantLogService.list().size() == 3
        crmTenantLogService.list(tenant: 0).size() == 2
        crmTenantLogService.list(tenant: 1).size() == 1
        crmTenantLogService.list(user: 'test').size() == 0
        crmTenantLogService.list(user: 'me').size() == 1
        crmTenantLogService.list(username: 'you').size() == 1
        crmTenantLogService.list(category: 'test').size() == 3
        crmTenantLogService.list(category: 'foo').size() == 0
        crmTenantLogService.list(from: new Date() - 7, to: new Date()).size() == 3
        crmTenantLogService.list(from: new Date() + 1).size() == 0
    }
}
