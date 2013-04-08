<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'crmTenant.label', default: 'Tenant')}"/>
    <title><g:message code="crmTenant.create.title" subtitle="crmTenant.create.subtitle"
                      args="[entityName, crmUser.name]" default="Create Tenant"/></title>
    <style type="text/css">
    .feature-body {
        min-height: 120px;
    }
    </style>
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

<g:form action="create" class="form-inline">

    <g:hiddenField name="account.id" value="${crmTenant.account?.id}"/>

    <div class="row-fluid">
        <div class="span8">

            <div class="row-fluid">
                <div class="control-group">
                    <label class="control-label"><g:message code="crmTenant.name.label"/></label>

                    <div class="controls">
                        <g:textField name="name" maxlength="40" autofocus="" required="" value="${crmTenant.name}"
                                     placeholder="${message(code: 'crmTenant.name.placeholder')}"/>
                        <label class="checkbox">
                            <g:checkBox name="defaultTenant" value="true" checked="${!crmUser.defaultTenant}"/>
                            <g:message code="crmUser.defaultTenant.help"/>
                        </label>
                    </div>
                </div>
            </div>

            <hr/>

            <g:set var="availableFeatures" value="${allFeatures.findAll { !it.hidden }.sort { it.name }}"/>

            <g:each in="${availableFeatures.collate(3)}" var="row">
                <div class="row-fluid">
                    <ul class="thumbnails">
                        <g:each in="${row}" var="f">
                            <li class="span4">
                                <div class="thumbnail">

                                    <div class="feature-body">
                                        <h5 class="center">
                                            ${message(code: 'feature.' + f.name + '.label', default: f.name)}
                                        </h5>

                                        <p>${message(code: 'feature.' + f.name + '.description', default: f.description)}</p>
                                    </div>

                                    <g:set var="readme" value="${false}"/>

                                    <div class="form-actions" style="margin-bottom: 0;">
                                        <g:if test="${f.required}">
                                            <input type="hidden" id="feature-${f.name}-required" name="features"
                                                   value="${f.name}"/>
                                            <label class="checkbox">
                                                <g:checkBox id="feature-${f.name}-checkbox" name="feature"
                                                            value="${f.name}"
                                                            disabled="disabled"
                                                            checked="${true}"/>
                                                Aktivera funktionen
                                            </label>
                                        </g:if>
                                        <g:else>
                                            <label class="checkbox">
                                                <g:checkBox id="feature-${f.name}-checkbox" name="features"
                                                            value="${f.name}"
                                                            checked="${(crmTenant.id == null) || f.required || features?.contains(f.name)}"/>
                                                Aktivera funktionen
                                            </label>
                                        </g:else>
                                    </div>
                                </div>
                            </li>
                        </g:each>
                    </ul>
                </div>
            </g:each>

        </div>

        <div class="span4">
            <tt:html name="tenant-create-help"></tt:html>
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
