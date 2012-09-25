<html>
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'crmRole.label', default: 'Role')}" />
  <title><g:message code="crmRole.edit.title" args="[entityName, crmRole]" /></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="crmRole.edit.title" args="[entityName, crmRole]" /></h1>

  <g:hasErrors bean="${crmRole}">
    <g:content tag="crm-alert">
      <div class="errors">
        <g:renderErrors bean="${crmRole}" as="list"/>
      </div>
    </g:content>
  </g:hasErrors>

    <g:form name="inputForm">
      <input type="hidden" name="id" value="${crmRole?.id}" />
      <input type="hidden" name="version" value="${crmRole?.version}" />

      <fieldset>
        <legend><g:message code="crmRole.name.label" default="Name"/></legend>

        <label for="name"><g:message code="crmRole.name.label"/></label>
        <g:textField name="name" value="${crmRole.name}"/>
      </fieldset>

      <fieldset>
        <legend><g:message code="crmRole.permissions.label" default="Permissions"/></legend>
        <label><g:message code="crmRole.permissions.label" /></label>
        <g:each in="${crmRole.permissions}" var="p">
          <g:textField name="permission" size="100" maxlength="255" value="${p}"/><br/>
        </g:each>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/>
      </fieldset>

      <div class="buttons">
        <crm:button class="positive" action="update" icon="disk" message="crmRole.button.save.label"/>
        <crm:button class="negative" action="delete" icon="delete" message="crmRole.button.delete.label" confirm="true"/>
      </div>

    </g:form>
  </div>

</body>
</html>
