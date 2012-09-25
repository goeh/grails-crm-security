<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmUser.label', default: 'User')}"/>
    <title><g:message code="crmUser.edit.title" args="[entityName, crmUser]"/></title>
</head>

<body>

<crm:header title="crmUser.edit.title" subtitle="${crmUser.name}" args="[entityName, crmUser]"/>

<g:hasErrors bean="${crmUser}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmUser}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="edit">

    <f:with bean="crmUser">

        <g:hiddenField name="id" value="${crmUser?.id}"/>
        <g:hiddenField name="version" value="${crmUser?.version}"/>

        <div class="row-fluid">
            <div class="span4">
                <div class="control-group ">
                    <label class="control-label" for="username"><g:message code="crmUser.username.label"
                                                                           default="User Name"/></label>

                    <div class="controls">
                        <g:textField name="username" disabled="disabled" value="${crmUser.username}"/>
                    </div>
                </div>
                <f:with bean="crmUser">
                    <f:field property="email" autofocus=""/>
                    <f:field property="name"/>
                    <f:field property="company"/>
                    <f:field property="telephone"/>
                </f:with>
            </div>

            <div class="span4">
                <f:with bean="crmUser">
                    <f:field property="address1"/>
                    <f:field property="address2"/>
                    <f:field property="postalCode"/>
                    <f:field property="city"/>
                    <f:field property="countryCode" label="register.country.label">
                        <g:countrySelect name="countryCode" value="${crmUser.countryCode}"
                                         noSelection="['': '']"/>
                    </f:field>
                </f:with>
            </div>

            <div class="span4">
                <f:with bean="crmUser">
                    <f:field property="enabled"/>
                    <f:field property="loginFailures"/>
                </f:with>
            </div>
        </div>

        <div class="form-actions">
            <crm:button visual="primary" icon="icon-ok icon-white" label="crmUser.button.update.label"/>
        </div>

    </f:with>

</g:form>

</body>
</html>
