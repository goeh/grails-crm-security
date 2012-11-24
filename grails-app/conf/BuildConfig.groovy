grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.repos.default = "crm"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn"
    repositories {
        grailsHome()
        grailsCentral()
        mavenRepo "http://labs.technipelago.se/repo/crm-releases-local/"
        mavenRepo "http://labs.technipelago.se/repo/plugins-releases-local/"
    }
    dependencies {
    }

    plugins {
        build(":tomcat:$grailsVersion",
                ":hibernate:$grailsVersion",
                ":release:2.0.4",
                ":rest-client-builder:1.0.2") {
            export = false
        }

        test(":spock:0.7") { export = false }
        test(":codenarc:0.17") { export = false }

        compile(":platform-core:1.0.M6") { excludes 'resources' }
        compile ":resources:1.2.RC2"
        compile ":cache:1.0.0"
        //compile ":cache-ehcache:1.0.0.M2"
        runtime ":jquery:1.8.3"
        runtime ":simple-captcha:0.8.5"

        compile "grails.crm:crm-core:latest.integration"
        runtime "grails.crm:crm-ui-bootstrap:latest.integration"
        runtime "grails.crm:crm-feature:latest.integration"
    }
}

