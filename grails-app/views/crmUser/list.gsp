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

<div class="row-fluid">
    <div class="span9">
        <table class="table table-striped">
            <thead>
            <tr>
                <g:sortableColumn property="username"
                                  title="${message(code: 'crmUser.username.label', default: 'Username')}"/>

                <g:sortableColumn property="name"
                                  title="${message(code: 'crmUser.name.label', default: 'Name')}"/>

                <g:sortableColumn property="email"
                                  title="${message(code: 'crmUser.email.label', default: 'Email')}"/>

                <g:sortableColumn property="postalCode"
                                  title="${message(code: 'crmUser.postalCode.label', default: 'Postal Code')}"/>
                <g:sortableColumn property="campaign"
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

                    <td>
                        ${fieldValue(bean: crmUser, field: "name")}
                    </td>
                    <td>
                        ${fieldValue(bean: crmUser, field: "email")}
                    </td>
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
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>

</div>

</body>
</html>
