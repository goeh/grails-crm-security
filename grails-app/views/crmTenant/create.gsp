<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'crmTenant.label', default: 'Account')}"/>
    <title><g:message code="crmTenant.create.title" subtitle="crmTenant.create.subtitle"
                      args="[entityName, crmUser.name]" default="Create Account"/></title>
</head>

<body>

<crm:header title="crmTenant.create.title" subtitle="crmTenant.create.subtitle"
            args="[entityName, crmUser.name]"/>

<g:hasErrors bean="${crmTenant}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmTenant}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="create">

    <div class="row-fluid">
        <div class="span6">

            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><g:message code="crmTenant.name.label"/></label>

                        <div class="controls">
                            <g:textField name="name" maxlength="40" autofocus="" required="" value="${crmTenant.name}"
                                         placeholder="${message(code: 'crmTenant.name.placeholder')}"/>
                        </div>
                    </div>
                </div>

                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><g:message code="crmUser.defaultTenant.label"/></label>

                        <div class="controls">
                            <label class="checkbox">
                                <g:checkBox name="defaultTenant" value="${!crmUser.defaultTenant}"/>
                                <g:message code="crmUser.defaultTenant.help"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <g:set var="availableFeatures" value="${allFeatures.findAll {!it.hidden}.sort {it.name}}"/>
            <g:if test="${availableFeatures}">
                <h3><g:message code="crmTenant.features.select.title"/></h3>

                <g:render template="features" model="${[result: availableFeatures]}"/>
            </g:if>
        </div>

        <div class="span6">
            <tt:html name="account-create-help"></tt:html>
        </div>
    </div>

    <div class="form-actions">
        <crm:button icon="icon-ok icon-white" visual="primary"
                    label="crmTenant.button.save.label" accesskey="S"/>
        <crm:button type="link" action="index" icon="icon-remove"
                    label="crmTenant.button.cancel.label"
                    accesskey="B"/>
    </div>

</g:form>

</body>
</html>
