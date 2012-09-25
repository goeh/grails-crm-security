<html>
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'crmRole.label', default: 'Role')}" />
  <title><g:message code="crmRole.show.title" args="[entityName, crmRole]"/></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="crmRole.show.title" args="[entityName, crmRole]"/></h1>

    <fieldset>
      <legend><g:message code="crmRole.name.label" default="Name"/></legend>
      <label><g:message code="crmRole.name.label" /></label>
      <span class="value">${fieldValue(bean:crmRole, field:'name')}</span>
    </fieldset>

    <fieldset>
      <legend><g:message code="crmRole.permissions.label" default="Permissions"/></legend>
      <label><g:message code="crmRole.permissions.label" /></label>
      <span class="value">${crmRole.permissions ? crmRole.permissions.join('\n').encodeAsHTML().replace('\n', '<br/>') : message(code:'crmRole.permissions.empty')}</span>
    </fieldset>

    <div class="buttons">
      <crm:link class="positive" action="edit" id="${crmRole.id}" icon="pencil" message="crmRole.button.edit.label"/>
    </div>
  </div>

</body>
</html>
