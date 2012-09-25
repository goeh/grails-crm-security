<html>
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'crmRole.label', default: 'Role')}" />
  <title><g:message code="crmRole.create.title" args="[entityName]"/></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="crmRole.create.title" args="[entityName]"/></h1>

  <g:hasErrors bean="${crmRole}">
    <g:content tag="crm-alert">
      <div class="errors">
        <g:renderErrors bean="${crmRole}" as="list"/>
      </div>
    </g:content>
  </g:hasErrors>

    <g:form action="save" name="inputForm">

      <fieldset>
        <legend><g:message code="crmRole.name.label" default="Name"/></legend>

        <label for="name"><g:message code="crmRole.name.label"/></label>
        <g:textField name="name" value="${crmRole.name}"/>
      </fieldset>

      <fieldset>
        <legend><g:message code="crmRole.permissions.label" default="Permissions"/></legend>
        <label><g:message code="crmRole.permissions.label" /></label>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/>
      </fieldset>

      <div class="buttons">
        <crm:button class="positive" action="save" icon="disk" message="crmRole.button.save.label"/>
      </div>

    </g:form>
  </div>

</body>
</html>
