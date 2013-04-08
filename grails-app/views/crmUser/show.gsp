<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmUser.label', default: 'User')}"/>
    <title><g:message code="crmUser.show.title" args="[entityName, crmUser]"/></title>
    <r:script>
        $(document).ready(function () {
            $('.crm-role').popover({placement: 'left', trigger: 'hover', delay: { show: 600, hide: 100 }});
        });
    </r:script>
</head>

<body>

<header class="page-header clearfix">
    <crm:user>
        <h1 class="pull-left">
            ${crmUser.name.encodeAsHTML()}
            <small>${crmUser.username?.encodeAsHTML()}</small>
            ${crmUser.enabled ? '' : '<i class="icon-ban-circle"></i>'}
        </h1>
    </crm:user>
</header>

<div class="tabbable">
    <ul class="nav nav-tabs">
        <li class="active"><a href="#main" data-toggle="tab"><g:message code="crmUser.tab.main.label"/></a>
        </li>

        <li><a href="#account" data-toggle="tab"><g:message code="crmAccount.label"/></a></li>

        <li><a href="#tenant" data-toggle="tab"><g:message code="crmTenant.label"/></a></li>

        <crm:pluginViews location="tabs" var="view">
            <crm:pluginTab id="${view.id}" label="${view.label}" count="${view.model?.totalCount}"/>
        </crm:pluginViews>
    </ul>

    <div class="tab-content">
        <div class="tab-pane active" id="main">
            <div class="row-fluid">
                <div class="span4">
                    <dl>
                        <dt><g:message code="crmUser.username.label" default="Username"/></dt>
                        <dd><g:fieldValue bean="${crmUser}" field="username"/></dd>

                        <dt><g:message code="crmUser.email.label" default="Email"/></dt>
                        <dd><g:fieldValue bean="${crmUser}" field="email"/></dd>

                        <dt><g:message code="crmUser.name.label" default="Name"/></dt>
                        <dd><g:fieldValue bean="${crmUser}" field="name"/></dd>

                        <g:if test="${crmUser.company}">
                            <dt><g:message code="crmUser.company.label" default="Company"/></dt>
                            <dd><g:fieldValue bean="${crmUser}" field="company"/></dd>
                        </g:if>

                        <g:if test="${crmUser.telephone}">
                            <dt><g:message code="crmUser.telephone.label" default="Telephone"/></dt>
                            <dd>${crmUser.telephone}</dd>
                        </g:if>
                    </dl>
                </div>

                <div class="span4">
                    <dl>
                        <g:if test="${crmUser.postalCode}">
                            <dt><g:message code="crmUser.postalCode.label" default="Postal code"/></dt>
                            <dd>${crmUser.postalCode}</dd>
                        </g:if>

                        <g:if test="${crmUser.campaign}">
                            <dt><g:message code="crmUser.campaign.label" default="Campaign"/></dt>
                            <dd>${crmUser.campaign}</dd>
                        </g:if>
                    </dl>
                </div>

                <div class="span4">
                    <dl>
                        <dt><g:message code="crmUser.status.label" default="Status"/></dt>
                        <dd>${message(code: 'crmUser.status.' + crmUser.status)}</dd>
                        <g:if test="${crmUser.loginFailures}">
                            <dt><g:message code="crmUser.loginFailures.label" default="Login failures"/></dt>
                            <dd>${crmUser.loginFailures}</dd>
                        </g:if>
                    </dl>
                </div>

            </div>

            <g:form>
                <g:hiddenField name="id" value="${crmUser?.id}"/>
                <div class="form-actions btn-toolbar">
                    <crm:button type="link" group="true" action="edit" id="${crmUser?.id}" visual="primary"
                                icon="icon-pencil icon-white"
                                label="crmUser.button.edit.label" permission="crmUser:edit"/>
                </div>
            </g:form>

        </div>

        <div class="tab-pane" id="account">
            <div class="row-fluid">
                <table class="table table-striped">
                    <thead>
                    <th>Namn</th>
                    <th>Fakturaadress</th>
                    <th>Status</th>
                    <th>Löper ut</th>
                    </thead>
                    <tbody>
                    <g:each in="${accountList}" var="account">
                        <tr class="${account.isActive() ? '' : 'disabled'}">
                            <td>
                                <g:link controller="crmAccountAdmin" action="edit"
                                        id="${account.id}">${account.name}</g:link>
                            </td>
                            <td>
                                ${(account.reference ?: crmUser.name).encodeAsHTML()}
                                ${account.address?.encodeAsHTML()}
                            </td>
                            <td>
                                ${message(code: 'crmAccount.status.' + account.getStatusText(), default: account.getStatusText())}
                            </td>
                            <td>
                                <g:if test="${account.expires}">
                                    <g:formatDate type="date" date="${account.expires}"/>
                                </g:if>
                                <g:else>
                                    Löpande
                                </g:else>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="tab-pane" id="tenant">
            <div class="row-fluid">

                <table class="table table-striped">
                    <thead>
                    <th style="width:16px;"></th>
                    <th>Id</th>
                    <th>Vynamn</th>
                    <th>Abonnemang</th>
                    <th>Roll</th>
                    </thead>
                    <tbody>
                    <g:each in="${tenantList}" var="tenant">
                        <tr class="${crm.isValidTenant(tenant: tenant.id, username: crmUser.username, { 'not-' })}disabled">
                            <td style="width:16px;">
                                <g:if test="${tenant.id == crmUser.defaultTenant}"><i class="icon-home"></i></g:if>
                            </td>
                            <td>
                                <g:link controller="crmTenantAdmin" action="edit"
                                        id="${tenant.id}">${tenant.id}</g:link>
                            </td>
                            <td>
                                <g:link controller="crmTenantAdmin" action="edit"
                                        id="${tenant.id}">${tenant.name.encodeAsHTML()}</g:link>
                            </td>
                            <td><g:link controller="crmAccountAdmin" action="edit"
                                        id="${tenant.account.id}">${tenant.account.encodeAsHTML()}</g:link></td>
                            <td>
                                <crm:userRoles tenant="${tenant.id}"
                                               username="${crmUser.username}">
                                    <div class="crm-role" data-title="Behörigheter"
                                         data-content="${it.permissions.sort().join(', ')}">
                                        ${it.role.encodeAsHTML()} <g:formatDate date="${it.expires}"/>
                                    </div>
                                </crm:userRoles>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>

        <crm:pluginViews location="tabs" var="view">
            <div class="tab-pane tab-${view.id}" id="${view.id}">
                <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
            </div>
        </crm:pluginViews>

    </div>
</div>

</body>
</html>
