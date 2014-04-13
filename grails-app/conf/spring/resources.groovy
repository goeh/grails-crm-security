beans = {
    crmSecurityDelegate(grails.plugins.crm.security.TestSecurityDelegate)
    grailsLinkGenerator(grails.plugins.crm.security.CrmThemeLinkGenerator, "http://www.domain.se", "/") { bean ->
        bean.autowire = 'byName'
    }
}
