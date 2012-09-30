<%@ page import="grails.plugins.crm.security.CrmUserPermission; grails.plugins.crm.security.CrmUserRole" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmTenant.label', default: 'Account')}"/>
    <title><g:message code="crmTenant.permissions.title" args="[entityName]"/></title>
    <r:script>
        $(document).ready(function () {
            $('.date').datepicker({weekStart:1});
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

    <div class="row-fluid">
        <div class="span9">

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
                    <g:set var="active" value="${expires == null || expires >= new java.sql.Date(new Date().clearTime().time)}"/>

                    <g:if test="${perm instanceof CrmUserRole}">
                        <tr class="${active ? '' : 'muted'}">
                            <td title="${perm.user.username?.encodeAsHTML()}">${perm.user.name?.encodeAsHTML()}</td>
                            <td title="${crm.permissionList(permission: perm.role.permissions, var: 'p', {p.label + ' '})}">
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
                                                permission="security:delete"/>
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
                                                permission="security:delete"/>
                                </g:unless>
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

        </div>

        <div class="span3">
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>Totalt aktiva</th>
                    <th>Antal</th>
                    <th>Max</th>
                </tr>
                </thead>
                <tbody>
                <tr class="${adminUsage[0] >= (adminUsage[1] ?: Integer.MAX_VALUE) ? 'error' : ''}">
                    <td>Administratörer</td><td>${adminUsage[0]}</td><td>${adminUsage[1] ? adminUsage[1] : ''}</td>
                </tr>
                <tr class="${userUsage[0] >= (userUsage[1] ?: Integer.MAX_VALUE) ? 'error' : ''}">
                    <td>Normala användare</td><td>${userUsage[0]}</td><td>${userUsage[1] ? userUsage[1] : ''}</td>
                </tr>
                <tr class="${guestUsage[0] >= (guestUsage[1] ?: Integer.MAX_VALUE) ? 'error' : ''}">
                    <td>Gästanvändare</td><td>${guestUsage[0]}</td><td>${guestUsage[1] ? guestUsage[1] : ''}</td>
                </tr>
                <crm:hasFeature feature="crmInvitation">
                    <tr class="${invitations.size() >= 25 ? 'error' : ''}">
                        <td>Inbjudningar</td><td>${invitations.size()}</td><td>${25}</td>
                    </tr>
                </crm:hasFeature>
                </tbody>
            </table>

        </div>
    </div>

    <div class="form-actions">
        <crm:hasFeature feature="crmInvitation">
            <g:if test="${saveButtonNeeded}">
                <crm:button action="permissions" visual="success" icon="icon-ok icon-white"
                            label="crmUserRole.button.update.label" permission="crmTenant:edit"/>
            </g:if>
            <crm:button type="url" visual="primary" icon="icon-share-alt icon-white" data-toggle="modal"
                        href="#modal-share-account"
                        label="crmTenant.button.share.label" permission="crmTenant:share"/>
        </crm:hasFeature>
    </div>

</g:form>

<div id="modal-share-account" class="modal hide">
    <div class="modal-header">
        <a href="#" class="close" data-dismiss="modal">&times;</a>

        <h3><g:message code="crmTenant.share.title" args="${[crmTenant.name]}"/></h3>
    </div>

    <div class="well">
        <g:form action="share">
            <input type="hidden" name="id" value="${crmTenant.id}"/>

            <div class="modal-body">
                <p><g:message code="crmTenant.share.message" args="${[crmTenant.name]}"/></p>
            </div>

            <div class="control-group">
                <label class="control-label">E-postadress</label>

                <div class="controls">
                    <input type="email" name="email" class="span4"
                           placeholder="E-postadress till den du vill bjuda in..."/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">Behörighet</label>

                <div class="controls">
                    <label class="radio inline"><g:radio value="guest" name="role" checked="checked"/>Läsa</label>
                    <label class="radio inline"><g:radio value="user" name="role"/>Ändra</label>
                    <label class="radio inline"><g:radio value="admin" name="role"/>Administrera</label>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">Meddelande (frivilligt)</label>

                <div class="controls">
                    <textarea name="msg" placeholder="Personligt meddelande..." class="span5" cols="40"
                              rows="3"></textarea>
                </div>
            </div>

            <div class="modal-footer">

                <crm:button visual="primary" icon="icon-ok icon-white" action="share"
                            label="crmTenant.button.share.confirm.yes" args="${[crmTenant.name]}"/>
                <crm:button type="url" icon="icon-remove" href="#"
                            label="crmTenant.button.share.confirm.no" args="${[crmTenant.name]}"
                            data-dismiss="modal"/>
            </div>
        </g:form>
    </div>
</div>

</body>
</html>
