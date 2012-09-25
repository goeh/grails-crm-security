<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'crmTenant.label', default: 'Account')}"/>
    <title><g:message code="crmTenant.index.title" subtitle="crmTenant.index.subtitle" args="[entityName, crmUser.name]" default="My Accounts"/></title>
</head>

<body>

<crm:header title="crmTenant.index.title" subtitle="crmTenant.index.subtitle" args="[entityName, crmUser.name]"/>

<g:if test="${crmTenantList}">
    <g:render template="list" model="[user:crmUser, result:crmTenantList]"/>
</g:if>
<g:else>
        <tt:html name="account-index-empty">

            <h2>Start by creating your first CRM account.</h2>

            <p>All information you enter into Grails CRM will be associated with an account. You can have more than one account per user.</p>

            <blockquote>Example: If you are running two different businesses and also want to keep track of your private contacts, you typically create three CRM accounts.</blockquote>

        </tt:html>
</g:else>

<div class="form-actions">
   <crm:button type="link" action="create" visual="success" icon="icon-file icon-white"
               label="crmTenant.button.create.label"/>
</div>

<tt:html name="account-index-main"></tt:html>

</body>
</html>
