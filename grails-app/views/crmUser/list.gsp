<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmUser.label', default: 'User')}"/>
    <title><g:message code="crmUser.list.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmUser.list.title" subtitle="Sökningen resulterade i ${totalCount} st användare"
            args="[entityName]">
</crm:header>

<table class="table table-striped">
    <thead>
    <tr>
        <crm:sortableColumn property="username"
                            title="${message(code: 'crmUser.username.label', default: 'Username')}"/>

        <crm:sortableColumn property="name"
                            title="${message(code: 'crmUser.name.label', default: 'Name')}"/>

        <crm:sortableColumn property="company"
                            title="${message(code: 'crmUser.company.label', default: 'Company')}"/>

        <crm:sortableColumn property="email"
                            title="${message(code: 'crmUser.email.label', default: 'Email')}"/>

        <crm:sortableColumn property="postalCode"
                            title="${message(code: 'crmUser.postalCode.label', default: 'Postal Code')}"/>
        <crm:sortableColumn property="campaign"
                            title="${message(code: 'crmUser.campaign.label', default: 'Campaign')}"/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" var="crmUser">
        <tr class="${crmUser.enabled ? '' : 'disabled'}">

            <td>
                <g:link action="show" id="${crmUser.id}">
                    ${fieldValue(bean: crmUser, field: "username")}
                </g:link>
                ${crmUser.enabled ? '' : '<i class="icon-ban-circle"></i>'}
            </td>

            <td>${fieldValue(bean: crmUser, field: "name")}</td>
            <td>${fieldValue(bean: crmUser, field: "company")}</td>
            <td>${fieldValue(bean: crmUser, field: "email")}</td>
            <td>${fieldValue(bean: crmUser, field: "postalCode")}</td>
            <td>${fieldValue(bean: crmUser, field: "campaign")}</td>

        </tr>
    </g:each>
    </tbody>
</table>

<crm:paginate total="${totalCount}"/>

<div class="form-actions btn-toolbar">
    <crm:selectionMenu visual="primary"/>
</div>

</body>
</html>
