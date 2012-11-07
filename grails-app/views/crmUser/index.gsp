<%@ page import="grails.plugins.crm.security.CrmUser" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'crmUser.label', default: 'User')}"/>
    <title><g:message code="crmUser.find.title" args="[entityName]"/></title>
</head>

<body>

<div class="row-fluid">
    <div class="span9">

        <crm:header title="crmUser.find.title" args="[entityName]"/>

        <g:form action="list">

            <f:with bean="cmd">

                <div class="row-fluid">
                    <div class="span6">
                        <f:field property="username" label="crmUser.username.label" input-autofocus=""/>
                        <f:field property="email" label="crmUser.email.label"/>
                        <f:field property="name" label="crmUser.name.label"/>
                        <f:field property="company" label="crmUser.company.label"/>
                    </div>

                    <div class="span6">
                        <f:field property="status">
                            <g:select name="status"
                                      from="${CrmUser.constraints.status.inList}"
                                      valueMessagePrefix="crmUser.status"
                                      value="${cmd.status}" noSelection="['': '']"
                                      class="input-medium"/>
                        </f:field>
                        <f:field property="postalCode" label="crmUser.postalCode.label"/>
                        <f:field property="campaign" label="crmUser.campaign.label"/>
                    </div>
                </div>

            </f:with>

            <div class="form-actions btn-toolbar">
                <crm:selectionMenu visual="primary">
                    <crm:button action="list" icon="icon-search icon-white" visual="primary"
                                label="crmUser.button.find.label"/>
                </crm:selectionMenu>
            </div>

        </g:form>
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>
