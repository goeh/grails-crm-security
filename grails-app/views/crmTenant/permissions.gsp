<%@ page import="grails.plugins.crm.security.CrmUserPermission; grails.plugins.crm.security.CrmUserRole" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmTenant.label', default: 'Account')}"/>
    <title><g:message code="crmTenant.permissions.title" args="[entityName]"/></title>
    <r:require module="datepicker"/>
    <r:script>
        $(document).ready(function () {
            $('.date').datepicker({weekStart: 1});
            $("#modal-share input:radio:first").attr('checked', 'checked');
        });
    </r:script>
</head>

<body>

<crm:header title="crmTenant.permissions.title" subtitle="${crmTenant.name.encodeAsHTML()}"
            args="[entityName, crmTenant]"/>

<g:hasErrors bean="${errorBean}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${errorBean}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:set var="saveButtonNeeded" value="${false}"/>

<g:form action="permissions">

    <g:hiddenField name="id" value="${crmTenant.id}"/>
    <g:hiddenField name="version" value="${crmTenant.version}"/>

    <table class="table table-bordered">
        <thead>
        <tr>
            <th><g:message code="crmUser.label" default="User"/></th>
            <th><g:message code="crmRole.label" default="Role"/></th>
            <th>Mål</th>
            <th><g:message code="crmAccount.expires.label" default="Expires"/></th>
            <th>Status</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${permissions}" var="perm" status="i">

            <g:set var="expires" value="${perm.expires ?: crmTenant.account.expires}"/>
            <g:set var="active"
                   value="${expires == null || expires >= new java.sql.Date(new Date().clearTime().time)}"/>

            <g:if test="${perm instanceof CrmUserRole}">
                <tr class="${active ? '' : 'muted'}">
                    <td title="${perm.user.username?.encodeAsHTML()}">${perm.user.name?.encodeAsHTML()}</td>
                    <td title="${crm.permissionList(permission: perm.role.permissions, var: 'p', { p.label + ' ' })}">
                        ${message(code: 'crmRole.role.' + perm.toString() + '.label', default: perm.toString())}
                    </td>
                    <td>
                        ${crmTenant.name.encodeAsHTML()}
                    </td>
                    <td>
                        <g:if test="${perm.user == me}">
                            &nbsp;${formatDate(type: 'date', date: expires, style: 'short')}
                        </g:if>
                        <g:else>
                            <div class="input-append date"
                                 data-date="${formatDate(format: 'yyyy-MM-dd', date: expires ?: new Date())}">
                                <g:textField name="role_expires_${perm.id}" class="input-small"
                                             value="${formatDate(type: 'date', date: expires, style: 'short')}"/><span
                                    class="add-on"><i class="icon-th"></i></span>
                            </div>
                            <g:set var="saveButtonNeeded" value="${true}"/>
                        </g:else>
                    </td>
                    <td>${active ? 'Aktiv' : 'Inaktiv'}</td>

                    <td style="text-align: right;">
                        <g:unless test="${perm.user == me}">
                            <crm:button type="link" action="deleteRole" id="${perm.id}"
                                        visual="danger" class="btn-mini" icon="icon-trash icon-white"
                                        label="crmUserRole.button.delete.label"
                                        confirm="crmUserRole.button.delete.confirm.message"
                                        permission="crmTenant:edit:${perm.role.tenantId}"/>
                        </g:unless>
                    </td>
                </tr>
            </g:if>

            <g:if test="${perm instanceof CrmUserPermission}">
                <tr class="${active ? '' : 'muted'}">
                    <td title="${perm.user.username?.encodeAsHTML()}">${perm.user.name?.encodeAsHTML()}</td>
                    <td>
                        <crm:permissionList permission="${perm.permissionsString}">
                            ${it.label}<br/>
                        </crm:permissionList>
                    </td>
                    <td></td>
                    <td>
                        <g:if test="${perm.user == me}">
                            &nbsp;${formatDate(type: 'date', date: expires, style: 'short')}
                        </g:if>
                        <g:else>
                            <div class="input-append date"
                                 data-date="${formatDate(format: 'yyyy-MM-dd', date: expires ?: new Date())}">
                                <g:textField name="perm_expires_${perm.id}" class="input-small"
                                             value="${formatDate(type: 'date', date: expires, style: 'short')}"/><span
                                    class="add-on"><i class="icon-th"></i></span>
                            </div>
                            <g:set var="saveButtonNeeded" value="${true}"/>
                        </g:else>
                    </td>
                    <td>${active ? 'Aktiv' : 'Inaktiv'}</td>

                    <td style="text-align: right;">
                        <g:unless test="${perm.user == me}">
                            <crm:button type="link" action="deletePermission" id="${perm.id}"
                                        visual="danger" class="btn-mini" icon="icon-trash icon-white"
                                        label="crmUserPermission.button.delete.label"
                                        confirm="crmUserPermission.button.delete.confirm.message"
                                        permission="crmTenant:edit:${perm.tenantId}"/>
                        </g:unless>
                    </td>
                </tr>
            </g:if>

        </g:each>

        <g:each in="${invitations}" var="inv">
            <g:set var="ttl" value="${grailsApplication.config.crm.invitation.expires ?: 30}"/>
            <tr class="${inv.active ? '' : 'disabled'}">
                <td>${inv.receiver?.encodeAsHTML()}</td>
                <td>${message(code: 'crmRole.role.' + inv.param + '.label', default: inv.param)}</td>
                <td>${inv.reference?.encodeAsHTML()}</td>
                <td><g:formatDate format="yyyy-MM-dd" date="${inv.dateCreated + ttl}"/></td>
                <td>${message(code:'crmInvitation.status.' + inv.status + '.label')}</td>
                <td style="text-align: right;"><crm:button type="link" action="deleteInvitation" id="${inv.id}"
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

    <div class="form-actions">
        <g:if test="${saveButtonNeeded}">
            <crm:button action="permissions" visual="success" icon="icon-ok icon-white"
                        label="crmUserRole.button.update.label" permission="crmTenant:edit:${crmTenant.id}"/>
        </g:if>
        <crm:isAllowedMoreInvitations>
            <crm:button type="url" visual="primary" icon="icon-share-alt icon-white" data-toggle="modal"
                        href="#modal-share"
                        label="crmTenant.button.share.label" permission="crmTenant:share:${crmTenant.id}"/>
        </crm:isAllowedMoreInvitations>
        <g:if test="${crmAccount}">
            <crm:button type="link" controller="crmAccount" action="index" icon="icon-briefcase"
                        label="crmAccount.index.label"/>
        </g:if>
    </div>

</g:form>

<crm:hasPlugin name="crm-invitation">
    <g:render template="/crmInvitation/share" plugin="crm-invitation" model="${[bean: crmTenant]}"/>
</crm:hasPlugin>

</body>
</html>
