<%@ page import="grails.plugins.crm.security.CrmTenant; grails.plugins.crm.core.TenantUtils" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmSettings.title" args="[cmd.name]" default="Settings"/></title>
</head>

<body>

<crm:header title="crmSettings.title" subtitle="${cmd.name.encodeAsHTML()}" args="[cmd.name]"/>

<g:hasErrors bean="${cmd}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${cmd}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="update">

    <div class="tabbable">

        <ul class="nav nav-tabs">
            <li class="active"><a href="#user" data-toggle="tab"><g:message code="crmSettings.tab.user.label"/></a></li>
            <li><a href="#misc" data-toggle="tab"><g:message code="crmSettings.tab.misc.label"/><crm:countIndicator
                    count="${roles.size()}"/></a></li>
            <crm:pluginViews location="tabs" var="view">
                <crm:pluginTab id="${view.id}" label="${view.label}" count="${view.model?.totalCount}"/>
            </crm:pluginViews>
        </ul>

        <div class="tab-content">

            <div class="tab-pane active" id="user">

                <div class="row-fluid">
                    <div class="span4">
                        <div class="row-fluid">

                            <f:with bean="${cmd}">
                                <f:field property="username" label="crmUser.username.label">
                                    <g:textField name="username-disabled" disabled="" value="${cmd.username}"
                                                 class="span10"/>
                                    <g:hiddenField name="username" value="${cmd.username}"/>
                                </f:field>
                                <f:field property="name" label="crmUser.name.label" autofocus="" input-class="span10"/>
                                <f:field property="company" label="crmUser.company.label" autofocus=""
                                         input-class="span10"/>
                                <f:field property="email" label="crmUser.email.label" input-class="span10"/>
                                <f:field property="telephone" label="crmUser.telephone.label" input-class="span6"/>
                                <f:field property="postalCode" label="crmUser.postalCode.label" input-class="span6"/>
                            </f:with>
                        </div>
                    </div>

                    <div class="span3">
                        <div class="row-fluid">

                            <f:field bean="${cmd}" property="defaultTenant" label="crmUser.defaultTenant.label">
                                <g:select from="${tenants}" name="defaultTenant" value="${cmd.defaultTenant}"
                                          optionKey="id" noSelection="${['': 'Välj startvy']}"/>
                            </f:field>

                            <f:field bean="${cmd}" property="startPage" label="crmUser.startPage.label">
                                <g:select name="startPage" value="${cmd.startPage}" from="${startPages.entrySet()}"
                                          optionKey="key" optionValue="value"/>
                            </f:field>
                        </div>
                    </div>

                    <div class="span5">
                        <div class="row-fluid">

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

                            <g:if test="${questions}">
                                <h4><g:message code="crmSettings.security.questions.title"
                                               default="Security questions"/></h4>
                                <g:each in="${0..2}" var="n">
                                    <div class="control-group">
                                        <g:select from="${questions}" name="q[${n}]" value="${answers[n]}" class="span6"
                                                  optionValue="${{ message(code: it) }}" noSelection="${['null': '']}"/>
                                        <g:textField name="a[${n}]" placeholder="${answers[n] ? '**********' : ''}"
                                                     class="span5"/>
                                    </div>
                                </g:each>
                            </g:if>
                        </div>
                    </div>
                </div>
            </div>

            <div class="tab-pane" id="misc">

                <table class="table table-bordered">
                    <thead>
                    <tr>
                        <th><g:message code="crmTenant.label" default="Tenant"/></th>
                        <th><g:message code="crmAccount.user.label" default="Owner"/></th>
                        <th><g:message code="crmRole.label" default="Role"/></th>
                        <th><g:message code="crmRole.expires.label" default="Expires"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${roles}" var="role">
                        <tr>
                            <g:set var="tenant"
                                   value="${CrmTenant.get(role.role.tenantId)}"/>
                            <td>${tenant.encodeAsHTML()}</td>
                            <td>${tenant.account.user.encodeAsHTML()}</td>
                            <td>${message(code: 'crmRole.role.' + role.toString() + '.label', default: role.toString())}</td>
                            <td>
                                <g:if test="${role.expires}">
                                    ${formatDate(date: role.expires, type: 'date')}
                                    <g:set var="today" value="${new Date()}"/>
                                    <div>
                                        <g:if test="${role.expires >= today}">
                                            (<g:message code="default.days.left.message"
                                                        args="${[role.expires - today]}"
                                                        default="{0} days left"/>)
                                        </g:if>
                                        <g:else>
                                            (<g:message code="crmTenant.expires.expired" default="Closed"/>)
                                        </g:else>
                                    </div>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>

            </div>

            <crm:pluginViews location="tabs" var="view">
                <div class="tab-pane tab-${view.id}" id="${view.id}">
                    <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
                </div>
            </crm:pluginViews>

        </div>
    </div>

    <div class="form-actions">
        <crm:button visual="primary" icon="icon-ok icon-white"
                    label="settings.button.update.label"/>
    </div>

</g:form>

</body>
</html>
