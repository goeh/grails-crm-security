package grails.plugins.crm.security

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Test Quartz jobs in the grails.plugins.crm.security package.
 */
class CrmSecurityJobsSpec extends grails.plugin.spock.IntegrationSpec {

    def crmAccountService
    def crmSecurityService
    def crmSecurityDelegate
    def grailsEventsRegistry
    def grailsEventsPublisher

    private CrmUser createUser(String name, int days) {
        CrmUser.withTransaction { tx ->
            def u = new CrmUser(username: name, name: name, email: name + "@test.com",
                    status: CrmUser.STATUS_NEW).save(failOnError: true, flush: true)
            // Simulate user was created a few days back.
            CrmUser.executeUpdate("update CrmUser set dateCreated = :d where username = :u",
                    [u: u.username, d: new Date() - days])
            crmSecurityDelegate.createUser(u.username, "secret")
            return u.refresh()
        }
    }

    def "test crmUserClenerJob"() {

        when: "add two users, one created 5 days ago and one created yesterday"
        def u1 = createUser("zombie", 5)
        def u2 = createUser("wombat", 1)

        then: "make sure dateCreated was set correct"
        u1.dateCreated <= (new Date() - 5)
        u2.dateCreated <= (new Date() - 1)

        when: "cleaner job is executed"
        def job = new CrmUserCleanerJob()
        job.crmSecurityService = crmSecurityService
        job.execute()

        then: "zombie was nuked but wombat is still around"
        CrmUser.findByUsername("zombie") == null
        CrmUser.findByUsername("wombat") != null
    }

    def "test crmRoleExpirationJob"() {
        given:
        def admin = crmSecurityService.createUser([username: "admin", name: "Administrator", email: "admin@test.com", password: "secret", status: CrmUser.STATUS_ACTIVE])
        def account = crmAccountService.createAccount([user: admin, status: CrmAccount.STATUS_ACTIVE], [crmUser: 3])
        def user1 = createUser("exp1", 130)
        def user2 = createUser("exp2", 60)
        def user3 = createUser("exp3", 10)
        def tenant
        crmSecurityService.runAs(admin.username) {
            tenant = crmSecurityService.createTenant(account, "Test")
            crmSecurityService.addUserRole(user1, 'user', new Date() - 100, tenant.id)
            crmSecurityService.addUserRole(user2, 'user', new Date() - 30, tenant.id)
            crmSecurityService.addUserRole(user3, 'user', new Date() + 20, tenant.id)
        }
        def job = new CrmRoleExpirationJob(grailsEventsPublisher: grailsEventsPublisher)
        def latch = new CountDownLatch(2)
        def result = []
        grailsEventsRegistry.on("crmUserRole", "expired") { data ->
            result << data.user
            latch.countDown()
            def deleteDate = new Date() - 90
            if (deleteDate > data.expires) {
                crmSecurityService.deleteRole(CrmUserRole.get(data.id))
            }
        }

        when:
        job.execute()
        latch.await(10L, TimeUnit.SECONDS)
        user1.refresh()
        user2.refresh()
        user3.refresh()

        then:
        latch.getCount() == 0
        result.contains('exp1') // Role is expired and was included in the check
        result.contains('exp2') // Role is expired and was included in the check
        !result.contains('exp3') // Role is active and was not included in the check

        user1.roles.size() == 0 // Role was expired and deleted
        user2.roles.size() == 1 // Role has expired but it's not yet deleted
        user3.roles.size() == 1 // Role is still active
    }

}
