<%@ page contentType="text/html;charset=UTF-8" %><!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmAccount.list.title" default="My Subscriptions"/></title>
</head>

<body>

<header class="page-header clearfix">
    <h1 class="pull-left">
        <g:message code="crmAccount.list.title" default="My Subscriptions"/>
        <small>${crmUser.username.encodeAsHTML()}</small>
    </h1>
</header>

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
                <g:link controller="crmAccount" action="edit"
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

<div class="form-actions">
    <crm:button type="link" action="create" visual="primary" icon="icon-file icon-white"
                label="crmAccount.create.label"/>
</div>

</body>
</html>