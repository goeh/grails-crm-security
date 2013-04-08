<%@ page contentType="text/html;charset=UTF-8" %><!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmAccount.create.title" default="Create New Subscription"/></title>
</head>

<body>

<header class="page-header clearfix">
    <h1 class="pull-left">
        <g:message code="crmAccount.create.title" default="Create New Subscription"/>
        <small>${crmUser.username.encodeAsHTML()}</small>
    </h1>
</header>

<g:hasErrors bean="${crmAccount}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmAccount}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="create">

    <f:with bean="${crmAccount}">

        <div class="row-fluid">

            <div class="span4">
                <div class="row-fluid">

                    <f:field property="name" input-class="span10" input-autofocus=""/>
                    <f:field property="address1" input-class="span10"/>
                    <!--
                    <f:field property="address2" input-class="span10"/>
                    <f:field property="address3" input-class="span10"/>
                    -->
                    <div class="control-group">
                        <label class="control-label"><g:message
                                code="crmAccount.postalAddress.label"/></label>

                        <div class="controls">
                            <g:textField name="postalCode" value="${crmAccount.postalCode}"
                                         class="span3"/>
                            <g:textField name="city" value="${crmAccount.city}"
                                         class="span7"/>
                        </div>
                    </div>
                    <!--
                    <f:field property="region" input-class="span10"/>
                    -->
                    <f:field property="countryCode">
                        <g:countrySelect name="countryCode" value="${crmAccount.countryCode}"
                                         noSelection="['': '']" class="span10"/>
                    </f:field>
                </div>
            </div>

            <div class="span4">
                <div class="row-fluid">
                    <f:field property="reference" input-class="span10"/>
                    <f:field property="email" input-class="span10"/>
                    <f:field property="telephone" input-class="span6"/>
                    <f:field property="ssn" input-class="span6"/>
                </div>
            </div>

            <div class="span4">
                <tt:html name="account-create-help"></tt:html>
            </div>
        </div>

    </f:with>

    <div class="form-actions">
        <crm:button action="create" visual="primary" icon="icon-ok icon-white"
                    label="crmAccount.button.save.label"/>
        <crm:button type="link" action="index" icon="icon-remove"
                    label="crmAccount.button.cancel.label"/>
    </div>
</g:form>

</body>
</html>