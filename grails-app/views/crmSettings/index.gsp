<%@ page import="grails.plugins.crm.security.CrmTenant; grails.plugins.crm.core.TenantUtils" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmSettings.title" args="[user.name]" default="Settings"/></title>
</head>

<body>

<crm:header title="crmSettings.title" subtitle="${user.name.encodeAsHTML()}" args="[user.name]"/>

<g:form action="update" class="form-horizontal">
    <g:hiddenField name="guid" value="${user.guid}"/>

    <div class="tabbable">

        <ul class="nav nav-tabs">
            <li class="active"><a href="#user" data-toggle="tab"><g:message code="crmSettings.tab.user.label"/></a></li>
            <li><a href="#security" data-toggle="tab"><g:message code="crmSettings.tab.security.label"/></a></li>
            <li><a href="#misc" data-toggle="tab"><g:message code="crmSettings.tab.misc.label"/><crm:countIndicator
                    count="${user.roles.size()}"/></a></li>
            <crm:pluginViews location="tabs" var="view">
                <li><a href="#${view.id}" data-toggle="tab">${view.label.encodeAsHTML()}</a></li>
            </crm:pluginViews>
        </ul>

        <div class="tab-content">
            <div class="tab-pane active" id="user">

                <div class="row-fluid">
                    <div class="span6">
                        <div class="control-group ">
                            <label class="control-label" for="username"><g:message code="crmUser.username.label"
                                                                                   default="User Name"/></label>

                            <div class="controls">
                                <g:textField name="username" disabled="disabled" value="${user.username}"/>
                            </div>
                        </div>
                        <f:with bean="${user}">
                            <f:field property="email" autofocus=""/>
                            <f:field property="name"/>
                            <f:field property="company"/>
                            <f:field property="telephone"/>
                        </f:with>
                    </div>

                    <div class="span6">
                        <f:with bean="${user}">
                            <f:field property="address1"/>
                            <f:field property="address2"/>
                            <f:field property="postalCode"/>
                            <f:field property="city"/>
                            <f:field property="countryCode" label="register.country.label">
                                <g:countrySelect name="countryCode" value="${user.countryCode}"
                                                 noSelection="['': '']"/>
                            </f:field>
                        </f:with>
                    </div>
                </div>

                <div class="form-actions">
                    <crm:button visual="primary" icon="icon-ok icon-white"
                                label="settings.button.update.label"/>
                </div>
            </div>

            <div class="tab-pane" id="security">
                <div class="row-fluid">
                    <div class="span5">
                        <div class="row-fluid">
                            <h4><g:message code="crmSettings.change.password.title" default="Change password"/></h4>

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

                    <div class="span7">
                        <div class="row-fluid">
                            <g:if test="${questions}">
                                <h4><g:message code="crmSettings.security.questions.title"
                                               default="Security questions"/></h4>
                                <g:each in="${0..2}" var="n">
                                    <div class="control-group">
                                        <g:select from="${questions}" name="q[${n}]" value="${answers[n]}" class="span5"
                                                  optionValue="${{message(code: it)}}" noSelection="${['null': '']}"/>
                                        <g:textField name="a[${n}]" placeholder="${answers[n] ? '**********' : ''}"
                                                     class="span5"/>
                                    </div>
                                </g:each>
                            </g:if>
                        </div>
                    </div>
                </div>

                <div class="form-actions">
                    <crm:button visual="primary" icon="icon-ok icon-white"
                                label="settings.button.update.label"/>
                </div>
            </div>

            <div class="tab-pane" id="misc">

                <table class="table table-bordered">
                    <thead>
                    <tr>
                        <th><g:message code="crmTenant.label" default="Tenant"/></th>
                        <th><g:message code="crmTenant.user.label" default="Owner"/></th>
                        <th><g:message code="crmRole.label" default="Role"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${user.roles}" var="role">
                        <tr>
                            <g:set var="account"
                                   value="${CrmTenant.get(role.role.tenantId)}"/>
                            <td>${account.encodeAsHTML()}</td>
                            <td>${account.user == user ? 'Jag' : account.user.name.encodeAsHTML()}</td>
                            <td>${message(code: 'crmRole.role.' + role.toString() + '.label', default: role.toString())}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>

            </div>
        </div>
    </div>

</g:form>

</body>
</html>
