<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmNamedPermission.label', default: 'CrmTaskType')}"/>
    <title><g:message code="crmNamedPermission.list.title" args="[entityName]"/></title>
    <r:script>
        $(document).ready(function() {
            $(".table-striped tr").hover(function() {
                $("i", $(this)).removeClass('hide');
            }, function() {
                $("i", $(this)).addClass('hide');
            });
        });
    </r:script>
</head>

<body>

<crm:header title="crmNamedPermission.list.title" args="[entityName]"/>

<div class="row-fluid">
    <div class="span9">

        <table class="table table-striped">
            <thead>
            <tr>

                <g:sortableColumn property="name"
                                  title="${message(code: 'crmNamedPermission.name.label', default: 'Name')}"/>
            </tr>
            </thead>
            <tbody>
            <g:each in="${result}" var="crmNamedPermission">
                <tr>
                    <td>
                        <g:link action="edit" id="${crmNamedPermission.id}">
                            ${fieldValue(bean: crmNamedPermission, field: "name")}
                        </g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <crm:paginate total="${totalCount}"/>

    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>
