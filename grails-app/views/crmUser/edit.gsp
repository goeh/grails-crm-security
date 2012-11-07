<%@ page import="grails.plugins.crm.security.CrmUser" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmUser.label', default: 'User')}"/>
    <title><g:message code="crmUser.edit.title" args="[entityName, crmUser]"/></title>
</head>

<body>

<crm:header title="crmUser.edit.title" subtitle="${crmUser.username}" args="[entityName, crmUser.name]"/>

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
                    <f:field property="postalCode"/>
                    <f:field property="countryCode" label="register.country.label">
                        <g:countrySelect name="countryCode" value="${crmUser.countryCode}"
                                         noSelection="['': '']"/>
                    </f:field>
                    <f:field property="campaign"/>
                </f:with>
            </div>

            <div class="span4">
                <f:with bean="crmUser">
                    <f:field property="status">
                        <g:select name="status"
                                  from="${CrmUser.constraints.status.inList}"
                                  valueMessagePrefix="crmUser.status"
                                  value="${crmUser.status}" class="input-medium"/>
                    </f:field>
                    <f:field property="loginFailures" input-class="input-medium"/>
                </f:with>

                <div class="control-group ">
                    <label class="control-label" for="password1">
                        <g:message code="crmSettings.password1.label" default="Password"/>
                    </label>

                    <div class="controls">
                        <g:passwordField name="password1" value="" class="span8"/>
                    </div>
                </div>

                <div class="control-group ">
                    <label class="control-label" for="password2">
                        <g:message code="crmSettings.password2.label" default="Repeat Password"/>
                    </label>

                    <div class="controls">
                        <g:passwordField name="password2" value="" class="span8"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="form-actions">
            <crm:button visual="primary" icon="icon-ok icon-white" label="crmUser.button.update.label"/>
            <g:unless test="${accountList}">
                <crm:button action="delete" visual="danger" icon="icon-trash icon-white"
                            label="crmUser.button.delete.label"
                            confirm="crmUser.button.delete.confirm.message"
                            permission="crmUser:delete"/>
            </g:unless>
        </div>

    </f:with>
</g:form>

</body>
</html>
