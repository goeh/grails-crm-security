<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmUser.label', default: 'User')}"/>
    <title><g:message code="crmUser.show.title" args="[entityName, crmUser]"/></title>
    <style type="text/css">
    li.perm {
        font-weight: bold;
    }
    </style>
</head>

<body>

<div class="row-fluid">
    <div class="span9">

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

                <g:each in="${tenantList}" var="tenant">
                    <li><a href="#t${tenant.id}" data-toggle="tab">${tenant.name.encodeAsHTML()}</a></li>
                </g:each>

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

                <g:each in="${tenantList}" var="tenant">
                    <div class="tab-pane" id="t${tenant.id}">
                        <div class="row-fluid">
                            <div class="span4">
                                <h4>Vyuppgifter</h4>
                                <dl>
                                    <dt>Id</dt>
                                    <dd>${tenant.id}</dd>
                                    <dt>Vynamn</dt>
                                    <dd>${tenant.name.encodeAsHTML()}</dd>
                                    <g:if test="${tenant.user.id != crmUser.id}">
                                        <dt>Ägare</dt>
                                        <dd><g:link action="show" id="${tenant.user.id}">
                                            ${tenant.user.name.encodeAsHTML()} (${tenant.user.username})
                                        </g:link>
                                        </dd>
                                    </g:if>
                                    <g:if test="${tenant.parent}">
                                        <dt>Ingår i</dt>
                                        <dd>${tenant.parent}</dd>
                                    </g:if>
                                    <dt><g:message code="crmTenant.dateCreated.label"/></dt>
                                    <dd><g:formatDate date="${tenant.dateCreated}" type="date"/></dd>
                                </dl>
                            </div>

                            <div class="span4">
                                <h4>Funktioner</h4>
                                <dl>
                                    <crm:eachFeature tenant="${tenant.id}" var="feature">
                                        <dt>${feature.name.encodeAsHTML()}</dt>
                                        <dd class="${feature.enabled ? 'enabled' : 'disabled'}"
                                            title="${feature.dump().encodeAsHTML()}">
                                            ${feature.description?.encodeAsHTML()}
                                            ${feature.enabled ? '' : '(disabled)'}
                                            <g:formatDate date="${feature.expires}"/>
                                        </dd>
                                    </crm:eachFeature>
                                </dl>

                                <h4>Parametrar</h4>
                                <dl>
                                    <g:each in="${tenant.options}" var="o">
                                        <dt>${o.key}</dt>
                                        <dd>${o.value}</dd>
                                    </g:each>
                                </dl>

                            </div>

                            <div class="span4">

                                <h4><g:message code="crmUser.roles.label" default="Roles"/></h4>
                                <dl>
                                    <crm:userRoles tenant="${tenant.id}"
                                                   username="${crmUser.username}">
                                        <dt>${it.role.encodeAsHTML()} <g:formatDate date="${it.expires}"/></dt>
                                        <dd>
                                            <ul>
                                                <g:each in="${it.permissions.sort()}" var="perm">
                                                    <li>${perm}</li>
                                                </g:each>
                                            </ul>
                                        </dd>
                                    </crm:userRoles>
                                </dl>

                                <h4><g:message code="crmUser.permissions.label" default="Individual permissions"/></h4>
                                <ul>
                                    <crm:userPermissions tenant="${tenant.id}"
                                                         username="${crmUser.username}">
                                        <li>${it.encodeAsHTML()}</li>
                                    </crm:userPermissions>
                                </ul>

                            </div>
                        </div>

                        <div class="form-actions">
                            <crm:button type="link" controller="crmTenantAdmin" action="edit" id="${tenant.id}"
                                        visual="primary" label="crmTenant.edit.label" icon="icon-pencil icon-white"
                                        permission="crmTenant:edit:${tenant.id}"/>
                        </div>
                    </div>
                </g:each>

                <crm:pluginViews location="tabs" var="view">
                    <div class="tab-pane tab-${view.id}" id="${view.id}">
                        <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
                    </div>
                </crm:pluginViews>

            </div>
        </div>

    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>
