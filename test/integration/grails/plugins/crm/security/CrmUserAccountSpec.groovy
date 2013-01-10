package grails.plugins.crm.security

/**
 * Test CrmSecurityService with focus on CrmUser and CrmAccount.
 */
class CrmUserAccountSpec extends grails.plugin.spock.IntegrationSpec {
    def crmSecurityService
    def grailsApplication

    def "create user without specifying status should create an inactive user"() {
        when:
        def user = crmSecurityService.createUser([username: "test1", name: "Test User", email: "test@test.com", password: "test123"])
        then:
        user != null
        user instanceof CrmUser
        user.status == CrmUser.STATUS_NEW
        !user.enabled
    }

    def "create user with enabled-parameter true should create an active user"() {
        when:
        def user = crmSecurityService.createUser([username: "test1", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        then:
        user != null
        user instanceof CrmUser
        user.status == CrmUser.STATUS_ACTIVE
        user.enabled
    }

    def "status parameter has higher priority than enabled parameter"() {
        when:
        def user = crmSecurityService.createUser([username: "test1", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_BLOCKED, enabled: true])
        then:
        user != null
        user instanceof CrmUser
        user.status == CrmUser.STATUS_BLOCKED
        !user.enabled
    }

    def "duplicate username is not allowed"() {
        when:
        def user = crmSecurityService.createUser([username: "test2", name: "Test User", email: "test@test.com", password: "test123"])
        then:
        user != null

        when:
        user = crmSecurityService.createUser([username: "test2", name: "Test User Duplicate", email: "info@technipelago.se", password: "test789"])
        then:
        thrown(Exception)
    }

    def "get user information"() {
        when:
        crmSecurityService.createUser([username: "test3", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])
        def info = crmSecurityService.getUserInfo("test3")

        then:
        info instanceof Map
        info.username == "test3"
        info.name == "Test User"
        info.email == "test@test.com"
        info.status == CrmUser.STATUS_ACTIVE
        info.enabled
    }

    def "get user instance"() {
        when:
        crmSecurityService.createUser([username: "test4", name: "Test User", email: "test@test.com", password: "test123"])
        def user = crmSecurityService.getUser("test4")

        then:
        user instanceof CrmUser
        user.username == "test4"
        user.name == "Test User"
        user.email == "test@test.com"
        user.status == CrmUser.STATUS_NEW
        !user.enabled
    }

    def "create account with no parameters"() {
        given:
        def result
        crmSecurityService.createUser([username: "createAccountUser1", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])

        when:
        crmSecurityService.runAs("createAccountUser1") {
            result = crmSecurityService.createAccount()
        }

        then:
        result.id != null
        result.name == "Test User"
        result.email == "test@test.com"
        result.telephone == null
        result.user.username == "createAccountUser1"
    }

    def "create account with parameters"() {
        given:
        def result
        crmSecurityService.createUser([username: "createAccountUser2", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])

        when:
        crmSecurityService.runAs("createAccountUser2") {
            result = crmSecurityService.createAccount(name: "My Account", telephone: "+46800000",
                    address1: "Box 123", postalCode: "12345", city: "Capital", reference: "test")
        }

        then:
        result.id != null
        result.name == "My Account"
        result.email == "test@test.com"
        result.telephone == "+46800000"
        result.address1 == "Box 123"
        result.postalCode == "12345"
        result.city == "Capital"
        result.reference == "test"
        result.user.username == "createAccountUser2"
    }

    def "create account with options"() {
        given:
        def result
        crmSecurityService.createUser([username: "createAccountUser3", name: "Test User", email: "test@test.com", password: "test123", status: CrmUser.STATUS_ACTIVE])

        when:
        crmSecurityService.runAs("createAccountUser3") {
            result = crmSecurityService.createAccount(name: "My Account", telephone: "+46800000",
                    address1: "Box 123", postalCode: "12345", city: "Capital", reference: "test",
                    options: [campaign: 'SPRING-12', createdBy: crmSecurityService.currentUser?.username])
        }

        then:
        result.id != null
        result.name == "My Account"
        result.user.username == "createAccountUser3"
        result.getOption('campaign') == 'SPRING-12'
        result.getOption('createdBy') == "createAccountUser3"

        when:
        result.setOption('count', 42)

        then:
        result.getOption('count') == 42

        when:
        result.setOption('count', 12345)

        then:
        result.getOption('count') == 12345

        when:
        result = CrmAccount.get(result.id)
        result.setOption('count', 7)

        then:
        result.getOption('count') == 7

        when:
        result.removeOption('count')

        then:
        result.getOption('count') == null
    }

}
