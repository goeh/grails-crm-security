grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    inherits("global") {}
    log "warn"
    legacyResolve false
    repositories {
        mavenRepo "http://repo.grails.org/grails/repo/"
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo "http://labs.technipelago.se/repo/crm-releases-local/"
    }
    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }

    plugins {
        build(":tomcat:$grailsVersion",
                ":hibernate:$grailsVersion",
                ":release:2.2.1",
                ":rest-client-builder:1.0.3") {
            export = false
        }
        test(":spock:0.7") {
            export = false
            exclude "spock-grails-support"
        }
        test(":codenarc:0.21") { export = false }
        test(":code-coverage:1.2.7") { export = false }
        test(":cache-ehcache:1.0.0") {
            excludes 'cache'
            export = false
        }
        compile(":platform-core:1.0.0") { excludes 'resources' }
        compile ":cache:1.1.1"
        compile ":simple-captcha:0.8.5"

        compile ":crm-core:2.0.3"
        compile ":crm-feature:2.0.1"
    }
}
