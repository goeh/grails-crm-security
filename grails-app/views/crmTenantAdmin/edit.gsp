<%@ page import="grails.plugins.crm.security.CrmUserPermission; grails.plugins.crm.security.CrmUserRole" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmTenant.label', default: 'Account')}"/>
    <title><g:message code="crmTenant.edit.label" args="[entityName, crmTenant]"/></title>
    <r:script>
    $(document).ready(function() {
        $('.date').datepicker({weekStart:1});

        $(".statistics").each(function(idx) {
            var feature = $(this).data("crm-feature");
            $(this).load("${createLink(controller: 'crmFeature', action: 'statistics')}", {id:"${crmTenant.id}", name:feature, template:"badge"});
        });
    });
    </r:script>
</head>

<body>

<header class="page-header">
    <h1>
        <g:message code="crmTenant.edit.title" default="Edit account {1}" args="${[entityName, crmTenant]}"/>
        <small>${crmTenant.user.username?.encodeAsHTML()}</small>
    </h1>
</header>

<g:hasErrors bean="${crmTenant}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmTenant}" var="error">
                <li <g:if
                            test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<div class="tabbable">

<ul class="nav nav-tabs">
    <li class="active"><a href="#main" data-toggle="tab">Vyuppgifter</a></li>
    <li><a href="#permissions" data-toggle="tab">Användare<crm:countIndicator
                count="${permissions.size() + invitations.size()}"/></a></li>
    <li><a href="#features" data-toggle="tab">Funktioner<crm:countIndicator
            count="${features.findAll {!it.hidden}.size()}"/></a></li>
</ul>

<div class="tab-content">
<div id="main" class="tab-pane active">

    <g:form action="edit">
        <g:hiddenField name="id" value="${crmTenant.id}"/>
        <g:hiddenField name="version" value="${crmTenant.version}"/>

        <div class="row-fluid">
            <f:with bean="crmTenant">

                <div class="span6">
                    <div class="row-fluid">
                        <f:field property="name" input-autofocus=""/>

                        <f:field property="user">
                            <g:link controller="crmUser" action="show" id="${crmTenant.user.id}"
                                    fragment="t${crmTenant.id}">
                                ${crmTenant.user.name.encodeAsHTML()} (${crmTenant.user.username})
                            </g:link>
                        </f:field>

                    </div>
                </div>

                <div class="span6">
                    <div class="row-fluid">

                        <f:field property="dateCreated">
                            <g:textField name="dateCreated"
                                         value="${formatDate(date: crmTenant.dateCreated, type: 'date')}"
                                         disabled="disabled" class="input-medium"/>
                        </f:field>

                        <f:field property="expires">
                            <div class="input-append date"
                                 data-date="${formatDate(format: 'yyyy-MM-dd', date: crmTenant.expires ?: new Date())}">
                                <g:textField name="expires"
                                             class="input-medium" size="10" placeholder="YYYY-MM-DD"
                                             value="${formatDate(type: 'date', date: crmTenant.expires, style: 'short')}"/><span
                                    class="add-on"><i class="icon-th"></i></span>
                            </div>
                            <g:if test="${crmTenant.expires}">
                                <g:set var="today" value="${new Date()}"/>
                                <div>
                                    <g:if test="${crmTenant.expires >= today}">
                                        (<g:message code="default.days.left.message"
                                                    args="${[crmTenant.expires - today]}"
                                                    default="{0} days left"/>)
                                    </g:if>
                                    <g:else>
                                        (<g:message code="crmTenant.expires.expired" default="Closed"/>)
                                    </g:else>
                                </div>
                            </g:if>
                        </f:field>

                    </div>
                </div>

            </f:with>

        </div>

        <div class="form-actions">
            <crm:button visual="primary" icon="icon-ok icon-white" label="crmTenant.button.update.label"
                        permission="crmTenant:edit:${crmTenant.id}"/>
            <crm:button action="reset" visual="danger" icon="icon-repeat icon-white"
                        label="crmTenant.button.reset.label" permission="crmTenant:reset:${crmTenant.id}"
                        confirm="crmTenant.button.reset.confirm.message"/>
            <crm:button type="link" icon="icon-remove" label="crmTenant.button.cancel.label"
                        action="index"/>
            <crm:hasPermission permission="crmTenant:delete:${crmTenant.id}">
                <g:link action="delete" id="${crmTenant.id}" class="text-error" style="margin-left: 10px;"
                        onclick="return confirm('${message(code: 'crmTenant.button.delete.confirm.message', default: 'Are you sure?')}')">
                    <g:message code="crmTenant.button.delete.label" default="Delete"/>
                </g:link>
            </crm:hasPermission>
        </div>

    </g:form>
</div>

<div id="permissions" class="tab-pane">

    <table class="table table-bordered">
        <thead>
        <tr>
            <th><g:message code="crmUser.label" default="User"/></th>
            <th><g:message code="crmRole.label" default="Role"/></th>
            <th>Mål</th>
            <th><g:message code="crmTenant.expires.label" default="Expires"/></th>
            <th>Status</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${permissions}" var="perm" status="i">

            <g:set var="expires" value="${perm.expires ?: crmTenant.expires}"/>
            <g:set var="active"
                   value="${expires == null || expires >= new java.sql.Date(new Date().clearTime().time)}"/>

            <g:if test="${perm instanceof CrmUserRole}">
                <tr class="${active ? '' : 'muted'}">
                    <td title="${perm.user.username?.encodeAsHTML()}">
                        <g:link controller="crmUser" action="show" id="${perm.user.id}">
                            ${perm.user.name?.encodeAsHTML()}
                            <small>&lt;${perm.user.email?.encodeAsHTML()}&gt;</small>
                        </g:link>
                    </td>
                    <td title="${crm.permissionList(permission: perm.role.permissions, var: 'p', {p.label + ' '})}">
                        ${message(code: 'crmRole.role.' + perm.toString() + '.label', default: perm.toString())}
                    </td>
                    <td>
                        ${crmTenant.name.encodeAsHTML()}
                    </td>
                    <td>
                        <div class="input-append date"
                             data-date="${formatDate(format: 'yyyy-MM-dd', date: expires ?: new Date())}">
                            <g:textField name="role_expires_${perm.id}" class="input-small"
                                         value="${formatDate(type: 'date', date: expires, style: 'short')}"/><span
                                class="add-on"><i class="icon-th"></i></span>
                        </div>
                        <g:set var="saveButtonNeeded" value="${true}"/>
                    </td>
                    <td>${active ? 'Aktiv' : 'Inaktiv'}</td>

                    <td style="text-align: right;">
                        <crm:button type="link" action="deleteRole" id="${perm.id}"
                                    visual="danger" class="btn-mini" icon="icon-trash icon-white"
                                    label="crmUserRole.button.delete.label"
                                    confirm="crmUserRole.button.delete.confirm.message"
                                    permission="crmTenant:edit:${perm.role.tenantId}"/>
                    </td>
                </tr>
            </g:if>

            <g:if test="${perm instanceof CrmUserPermission}">
                <tr class="${active ? '' : 'muted'}">
                    <td title="${perm.user.username?.encodeAsHTML()}">
                        <g:link controller="crmUser" action="show" id="${perm.user.id}">
                            ${perm.user.name?.encodeAsHTML()}
                            <small>&lt;${perm.user.email?.encodeAsHTML()}&gt;</small>
                        </g:link>
                    </td>
                    <td>
                        <crm:permissionList permission="${perm.permissionsString}">
                            ${it.label}<br/>
                        </crm:permissionList>
                    </td>
                    <td></td>
                    <td>
                        <div class="input-append date"
                             data-date="${formatDate(format: 'yyyy-MM-dd', date: expires ?: new Date())}">
                            <g:textField name="perm_expires_${perm.id}" class="input-small"
                                         value="${formatDate(type: 'date', date: expires, style: 'short')}"/><span
                                class="add-on"><i class="icon-th"></i></span>
                        </div>
                        <g:set var="saveButtonNeeded" value="${true}"/>
                    </td>
                    <td>${active ? 'Aktiv' : 'Inaktiv'}</td>

                    <td style="text-align: right;">
                        <crm:button type="link" action="deletePermission" params="${[id:perm.id, tenant:crmTenant.id]}"
                                    visual="danger" class="btn-mini" icon="icon-trash icon-white"
                                    label="crmUserPermission.button.delete.label"
                                    confirm="crmUserPermission.button.delete.confirm.message"
                                    permission="crmTenant:edit:${perm.tenantId}"/>
                    </td>
                </tr>
            </g:if>

        </g:each>

        <g:each in="${invitations}" var="inv">
            <tr>
                <td>${inv.receiver?.encodeAsHTML()}</td>
                <td>${message(code: 'crmRole.role.' + inv.param + '.label', default: inv.param)}</td>
                <td>${inv.reference?.encodeAsHTML()}</td>
                <td><g:formatDate format="yyyy-MM-dd" date="${inv.dateCreated + 30}"/></td>
                <td>Inbjudan skickad</td>
                <td style="text-align: right;"><crm:button type="link" action="deleteInvitation" params="${[id:inv.id, tenant:crmTenant.id]}"
                                                           visual="danger" class="btn-mini"
                                                           icon="icon-trash icon-white"
                                                           label="crmInvitation.button.delete.label"
                                                           confirm="crmInvitation.button.delete.confirm.message"
                                                           args="${[inv.receiver]}"
                                                           permission="crmInvitation:cancel"/></td>
            </tr>
        </g:each>
        </tbody>
    </table>

</div>

<div id="features" class="tab-pane">

    <div class="row-fluid">
        <div class="span5">

            <h2>Installerade funktioner</h2>

            <g:if test="${features}">
                <g:each in="${features}" var="f">
                    <div class="well well-small clearfix ${f.enabled ? 'enabled' : 'disabled'}">
                        <h3 class="${f.hidden ? 'muted' : ''}">
                            ${message(code: 'feature.' + f.name + '.label', default: f.name)}
                            <span class="statistics" data-crm-feature="${f.name}"></span>
                        </h3>

                        <div>${message(code: 'feature.' + f.name + '.description', default: f.description)}</div>

                        <a href="${createLink(controller: 'crmFeature', action: 'info', params: [id: crmTenant.id, name: f.name])}"
                           data-toggle="modal" data-target="#modal-feature-info">Läs mer...</a>

                        <g:unless test="${f.required}">
                            <crm:hasPermission permission="crmFeature:install:${crmTenant.id}">
                                <g:link controller="crmFeature" action="uninstall"
                                        params="${[id: crmTenant.id, name: f.name]}" class="pull-right">
                                    <i class="icon icon-trash"></i>
                                </g:link>
                            </crm:hasPermission>
                        </g:unless>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <p>Inga funktioner installerade.</p>
            </g:else>
        </div>

        <div class="span5">

            <h2>Ej installerade funktioner</h2>

            <g:if test="${moreFeatures}">
                <g:each in="${moreFeatures}" var="f">
                    <div class="well well-small clearfix">
                        <%--
                        <img src="${resource(dir: 'images/avtala', file: 'arrow-e-y.png')}" width="32" height="32"
                             class="pull-left" style="margin-right:10px;"/>
                        --%>
                        <h3>${message(code: 'feature.' + f.name + '.label', default: f.name)}</h3>

                        <div>${message(code: 'feature.' + f.name + '.description', default: f.description)}</div>

                        <g:link controller="crmFeature" action="info"
                                params="${[id: crmTenant.id, name: f.name]}">Läs mer...</g:link>

                        <crm:button type="link" controller="crmFeature" action="install"
                                    params="${[id: crmTenant.id, name: f.name]}"
                                    visual="success btn-mini" icon="icon-download icon-white" class="pull-right"
                                    label="Installera" permission="crmFeature:install:${crmTenant.id}"/>

                    </div>
                </g:each>
            </g:if>
            <g:else>
                <p>Alla tillgängliga funktioner är installerade.</p>
            </g:else>
            <g:if test="${features}">
                <div class="alert alert-info">
                    <p><span
                            class="badge badge-success">543</span> Grön etikett betyder att funktionen används regelbundet.
                    </p>

                    <p><span
                            class="badge badge-warning">21</span> Gul etikett betyder att funktionen används mer sällan.
                    </p>

                    <p><span
                            class="badge badge-important">0</span> Röd etikett betyder att funktionen inte används alls.
                    </p>

                    <p><span
                            class="badge">?</span> Grå etikett betyder att användningen inte kan mätas med precision.
                    </p>

                    <p>Siffran i etiketten anger hur många objekt (t.ex. företag eller avtal) som för närvarande hanteras av funktionen i den aktuella vyn.</p>
                </div>
            </g:if>

        </div>

        <div class="span2">
            <crm:pluginViews location="sidebar" var="view">
                <div id="${view.id}" class="row-fluid">
                    <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
                </div>
            </crm:pluginViews>
        </div>

    </div>

</div>
</div>
</div>

<div id="modal-delete-account" class="modal hide">
    <div class="modal-header">
        <a href="#" class="close" data-dismiss="modal">&times;</a>

        <h3><g:message code="crmTenant.delete.confirm.title" args="${[crmTenant.name]}"/></h3>
    </div>

    <div class="modal-body">
        <p><g:message code="crmTenant.delete.confirm.message" args="${[crmTenant.name]}"/></p>
    </div>

    <div class="modal-footer">
        <g:form action="delete">
            <input type="hidden" name="id" value="${crmTenant.id}"/>

            <crm:button type="url" visual="primary" icon="icon-remove icon-white" href="#"
                        label="crmTenant.delete.confirm.no" args="${[crmTenant.name]}"
                        data-dismiss="modal"/>
            <crm:button visual="danger" icon="icon-trash icon-white" action="delete"
                        label="crmTenant.delete.confirm.yes" args="${[crmTenant.name]}"/>
        </g:form>
    </div>
</div>

<div id="modal-feature-info" class="modal hide">

    <div class="modal-header">
        <a class="close" data-dismiss="modal">×</a>

        <h3><g:message code="crmFeature.info.title" default="Information"/></h3>
    </div>

    <div class="modal-body"></div>

    <div class="modal-footer">
        <a href="#" class="btn btn-primary" data-dismiss="modal"><i class="icon-ok icon-white"></i> <g:message
                code="default.button.close.label" default="Close"/></a>
    </div>

</div>

</body>
</html>

