<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmAccount.index.title" default="Subscriptions"/></title>
</head>

<body>

<crm:header title="crmAccount.index.title"/>

<h2>Senast skapade</h2>

<table class="table table-striped">
    <thead>
    <tr>

        <g:sortableColumn property="name" title="${message(code: 'crmAccount.name.label', default: 'Name')}"/>
        <g:sortableColumn property="user.name" title="${message(code: 'crmAccount.user.label', default: 'Owner')}"/>
        <g:sortableColumn property="user.username"
                          title="${message(code: 'crmUser.username.label', default: 'Username')}"/>
        <g:sortableColumn property="user.email" title="${message(code: 'crmUser.email.label', default: 'Email')}"/>
        <g:sortableColumn property="dateCreated"
                          title="${message(code: 'crmAccount.dateCreated.label', default: 'Created')}"/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${recent}" var="crmAccount">
        <tr>
            <td><g:link action="edit" id="${crmAccount.id}">${fieldValue(bean: crmAccount, field: "name")}</g:link></td>
            <td><g:link action="edit"
                        id="${crmAccount.id}">${fieldValue(bean: crmAccount, field: "user.name")}</g:link></td>
            <td><g:link action="edit"
                        id="${crmAccount.id}">${fieldValue(bean: crmAccount, field: "user.username")}</g:link></td>
            <td><g:link action="edit"
                        id="${crmAccount.id}">${fieldValue(bean: crmAccount, field: "user.email")}</g:link></td>
            <td><g:formatDate type="datetime" style="short" date="${crmAccount.dateCreated}"/></td>
        </tr>
    </g:each>
    </tbody>
</table>

</body>
</html>
