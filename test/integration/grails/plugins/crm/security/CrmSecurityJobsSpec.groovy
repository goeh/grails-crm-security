package grails.plugins.crm.security

import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder

/**
 * Test Quartz jobs in the grails.plugins.crm.security package.
 */
class CrmSecurityJobsSpec extends grails.test.spock.IntegrationSpec {

    def crmSecurityService
    def crmSecurityDelegate

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
}
