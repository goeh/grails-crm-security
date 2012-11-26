<%@ page import="grails.plugins.crm.security.CrmUserPermission; grails.plugins.crm.security.CrmUserRole" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmTenant.label', default: 'Account')}"/>
    <title><g:message code="crmTenant.edit.label" args="[entityName]"/></title>
    <r:script>
    $(document).ready(function() {
        $(".statistics").each(function(idx) {
            var feature = $(this).data("crm-feature");
            $(this).load("${createLink(controller: 'crmFeature', action: 'statistics')}", {id:"${crmTenant.id}", name:feature, template:"badge"});
        });
    });
    </r:script>
    <style type="text/css">
    .feature-body {
        min-height: 120px;
    }
    </style>
</head>

<body>

<crm:header title="crmTenant.edit.title" subtitle="${crmTenant.account.encodeAsHTML()}"
            args="[entityName, crmTenant]"/>

<div class="tabbable">

    <ul class="nav nav-tabs">
        <li class="active"><a href="#main" data-toggle="tab">Vyuppgifter</a></li>
        <li><a href="#features" data-toggle="tab">Funktioner<crm:countIndicator
                count="${features.size()}"/></a></li>
    </ul>

    <div class="tab-content">
        <div id="main" class="tab-pane active">
            <g:form action="edit" id="${crmTenant.id}">
                <g:hiddenField name="version" value="${crmTenant.version}"/>

                <g:hasErrors bean="${crmTenant}">
                    <crm:alert class="alert-error">
                        <ul>
                            <g:eachError bean="${crmTenant}" var="error">
                                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                                        error="${error}"/></li>
                            </g:eachError>
                        </ul>
                    </crm:alert>
                </g:hasErrors>

                <div class="row-fluid">
                    <f:with bean="crmTenant">

                        <div class="span7">

                            <f:field property="name" input-autofocus=""/>

                            <crm:hasPlugin name="crm-agreement">
                                <div class="control-group">
                                    <label class="control-label">Använd kostnader i avtalsbilden</label>

                                    <div class="controls">
                                        <label class="checkbox inline">
                                            <g:radio value="true" name="showCosts"
                                                     checked="${showCosts}"/>
                                            Ja
                                        </label>
                                        <label class="checkbox inline">
                                            <g:radio value="false" name="showCosts"
                                                     checked="${!showCosts}"/>
                                            Nej
                                        </label>
                                    </div>
                                </div>

                                <div class="control-group">
                                    <label class="control-label">Antal parter för avtal</label>

                                    <div class="controls">
                                        <label class="checkbox">
                                            <g:radio value="false" name="partner2"
                                                     checked="${!partner2}"/>
                                            Använd en avtalspart (köpare ELLER säljare)
                                        </label>
                                        <label class="checkbox">
                                            <g:radio value="true" name="partner2"
                                                     checked="${partner2}"/>
                                            Använd två avtalsparter (köpare OCH säljare)
                                        </label>
                                    </div>
                                </div>
                            </crm:hasPlugin>

                        </div>

                        <div class="span5">

                            <f:field property="account" label="crmTenant.account.label">
                                <g:textField name="account" value="${crmTenant.account}" disabled="disabled"
                                             class="span6"/>
                            </f:field>

                            <f:field property="user" label="crmTenant.user.label">
                                <g:textField name="user" value="${crmTenant.user.name}" disabled="disabled"
                                             class="span6"/>
                            </f:field>

                            <f:field property="dateCreated">
                                <g:textField name="dateCreated"
                                             value="${formatDate(date: crmTenant.dateCreated, type: 'date')}"
                                             disabled="disabled"
                                             class="span6"/>
                            </f:field>
                        </div>

                    </f:with>

                </div>

                <div class="form-actions">
                    <crm:button visual="primary" icon="icon-ok icon-white" label="crmTenant.button.update.label"
                                permission="crmTenant:edit:${crmTenant.id}"/>
                    <%--
                    <crm:button action="reset" visual="danger" icon="icon-repeat icon-white"
                                label="crmTenant.button.reset.label" permission="crmTenant:reset:${crmTenant.id}"
                                confirm="crmTenant.button.reset.confirm.message"/>
                    --%>
                    <crm:button type="link" icon="icon-remove" label="crmTenant.button.cancel.label"
                                action="index"/>
                    <crm:hasPermission permission="crmTenant:delete:${crmTenant.id}">
                        <g:link action="delete" id="${crmTenant.id}" class="text-error" style="margin-left: 10px;"
                                onclick="return confirm('${message(code: 'crmTenant.button.delete.confirm.message', default: 'Are you sure?')}')">
                            <g:message code="crmTenant.button.delete.label" default="Delete"/>
                        </g:link>
                    </crm:hasPermission>
                </div>

            </g:form>
        </div>

        <div id="features" class="tab-pane">

            <div class="row-fluid">
                <div class="span8">

                    <g:each in="${features.collate(3)}" var="row">
                        <div class="row-fluid">
                            <ul class="thumbnails">
                                <g:each in="${row}" var="f">
                                    <li class="span4">
                                        <g:render template="feature"
                                                  model="${[crmTenant: crmTenant, f: f, installed: installed]}"/>
                                    </li>
                                </g:each>
                            </ul>
                        </div>
                    </g:each>

                </div>

                <div class="span4">
                    <g:if test="${installed}">
                        <div class="alert alert-info">
                            <p><span
                                    class="badge badge-success">543</span> Grön etikett betyder att funktionen används regelbundet.
                            </p>

                            <p><span
                                    class="badge badge-warning">21</span> Gul etikett betyder att funktionen används mer sällan.
                            </p>

                            <p><span
                                    class="badge badge-important">0</span> Röd etikett betyder att funktionen inte används alls.
                            </p>

                            <p><span
                                    class="badge">?</span> Grå etikett betyder att användningen inte kan mätas med precision.
                            </p>

                            <p>Siffran i etiketten anger hur många objekt (t.ex. företag eller avtal) som för närvarande hanteras av funktionen i den aktuella vyn.</p>
                        </div>
                    </g:if>
                </div>

            </div>
        </div>

    </div>
</div>

<div id="modal-delete-tenant" class="modal hide">
    <div class="modal-header">
        <a href="#" class="close" data-dismiss="modal">&times;</a>

        <h3><g:message code="crmTenant.delete.confirm.title" args="${[crmTenant.name]}"/></h3>
    </div>

    <div class="modal-body">
        <p><g:message code="crmTenant.delete.confirm.message" args="${[crmTenant.name]}"/></p>
    </div>

    <div class="modal-footer">
        <g:form action="delete">
            <input type="hidden" name="id" value="${crmTenant.id}"/>

            <crm:button type="url" visual="primary" icon="icon-remove icon-white" href="#"
                        label="crmTenant.delete.confirm.no" args="${[crmTenant.name]}"
                        data-dismiss="modal"/>
            <crm:button visual="danger" icon="icon-trash icon-white" action="delete"
                        label="crmTenant.delete.confirm.yes" args="${[crmTenant.name]}"/>
        </g:form>
    </div>
</div>

<div id="modal-feature-info" class="modal hide">

    <div class="modal-header">
        <a class="close" data-dismiss="modal">×</a>

        <h3><g:message code="crmFeature.info.title" default="Information"/></h3>
    </div>

    <div class="modal-body"></div>

    <div class="modal-footer">
        <a href="#" class="btn btn-primary" data-dismiss="modal"><i class="icon-ok icon-white"></i> <g:message
                code="default.button.close.label" default="Close"/></a>
    </div>

</div>

</body>
</html>

