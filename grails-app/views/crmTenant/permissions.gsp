<%@ page import="grails.plugins.crm.security.CrmUserPermission; grails.plugins.crm.security.CrmUserRole" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmTenant.label', default: 'Account')}"/>
    <title><g:message code="crmTenant.permissions.title" args="[entityName]"/></title>
    <r:script>
        $(document).ready(function () {
        });
    </r:script>
</head>

<body>

<crm:header title="crmTenant.permissions.title" subtitle="${crmTenant.name.encodeAsHTML()}"
            args="[entityName, crmTenant]"/>

<table class="table table-bordered">
    <thead>
    <tr>
        <th>Användare</th>
        <th>Roll</th>
        <th>Mål</th>
        <th>Gäller t.o.m</th>
        <th>Status</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${permissions}" var="perm">
        <tr>
            <td>${perm.user.name}</td>
            <g:if test="${perm instanceof CrmUserRole}">
                <td>${message(code: 'crmRole.role.' + perm.toString() + '.label', default: perm.toString())}</td>
                <td title="${crm.permissionList(permission: perm.role.permissions, var:'p', {p.label + ' '})}">
                    ${crmTenant.name.encodeAsHTML()}
                </td>
                <td><g:formatDate type="date" date="${perm.expires}" style="short"/></td>
                <td>${perm.expires == null || perm.expires > new Date() ? 'Aktiv' : 'Inaktiv'}</td>

                <td style="text-align: right;">
                    <g:unless test="${perm.user == me}">
                        <crm:button type="link" action="deleteRole" id="${perm.id}"
                                    visual="danger" class="btn-mini" icon="icon-trash icon-white"
                                    label="crmUserRole.button.delete.label"
                                    confirm="crmUserRole.button.delete.confirm.message"
                                    permission="security:delete"/>
                    </g:unless>
                </td>
            </g:if>
            <g:if test="${perm instanceof CrmUserPermission}">
                <td></td>
                <td>
                    <crm:permissionList permission="${perm.permissionsString}">
                        ${it.label}<br/>
                    </crm:permissionList>
                </td>
                <td><g:formatDate type="date" date="${perm.expires}" style="short"/></td>
                <td>Aktiv</td>

                <td style="text-align: right;">
                    <g:unless test="${perm.user == me}">
                        <crm:button type="link" action="deletePermission" id="${perm.id}"
                                    visual="danger" class="btn-mini" icon="icon-trash icon-white"
                                    label="crmUserPermission.button.delete.label"
                                    confirm="crmUserPermission.button.delete.confirm.message"
                                    permission="security:delete"/>
                    </g:unless>
                </td>
            </g:if>
        </tr>
    </g:each>

    <g:each in="${invitations}" var="inv">
        <tr>
            <td>${inv.receiver?.encodeAsHTML()}</td>
            <td>${message(code: 'crmRole.role.' + inv.param + '.label', default: inv.param)}</td>
            <td>${inv.reference?.encodeAsHTML()}</td>
            <td><g:formatDate format="yyyy-MM-dd" date="${inv.dateCreated + 30}"/></td>
            <td>Inbjudan skickad</td>
            <td style="text-align: right;"><crm:button type="link" action="deleteInvitation" id="${inv.id}"
                            visual="danger" class="btn-mini" icon="icon-trash icon-white"
                            label="crmInvitation.button.delete.label"
                            confirm="crmInvitation.button.delete.confirm.message"
                            args="${[inv.receiver]}"
                            permission="crmInvitation:cancel"/></td>
        </tr>
    </g:each>
    </tbody>
</table>

<crm:hasFeature feature="crmInvitation">
    <div class="form-actions">
        <crm:button type="url" visual="primary" icon="icon-share-alt icon-white" data-toggle="modal"
                    href="#modal-share-account"
                    label="crmTenant.button.share.label" permission="crmTenant:share"/>
    </div>
</crm:hasFeature>



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
