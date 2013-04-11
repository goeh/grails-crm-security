<%@ page import="grails.plugins.crm.core.WebUtils; grails.plugins.crm.security.CrmTenant" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmAccount.edit.title" args="[crmAccount.name]" default="Subscription"/></title>
</head>

<body>

<header class="page-header clearfix">
    <h1 class="pull-left">
        <g:message code="crmAccount.edit.title" default="Subscription" args="[crmAccount.name]"/>
        <small>${crmAccount.encodeAsHTML()}</small>
    </h1>

    <g:set var="expireDays" value="${crmAccount.expires ? crmAccount.expires - new Date() : Integer.MAX_VALUE}"/>

    <h4 class="pull-right ${expireDays < 45 ? (expireDays < 5 ? 'alert-red' : 'alert-yellow') : 'muted'}">
        <g:if test="${crmAccount.expires}">
            <g:if test="${expireDays >= 0}">
                Abonnemanget löper ut
                <g:formatDate type="date" date="${crmAccount.expires}" style="long"/>
                (<g:message code="default.days.left.message"
                            args="${[expireDays]}"
                            default="{0} days left"/>)
            </g:if>
            <g:else>
                <g:message code="crmAccount.expires.expired"
                           default="Abonnemanget har upphört!"/></span>
            </g:else>
        </g:if>
        <g:else>
            Abonnemanget gäller tills vidare
        </g:else>
    </h4>
</header>

<g:hasErrors bean="${crmAccount}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmAccount}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form>
<g:hiddenField name="id" value="${crmAccount.id}"/>
<g:hiddenField name="version" value="${crmAccount.version}"/>

<div class="tabbable">

    <ul class="nav nav-tabs">
        <li class="active"><a href="#account" data-toggle="tab"><g:message code="crmAccount.tab.account.label"/></a>
        </li>
        <g:if test="${tenantList}">
            <li><a href="#tenants" data-toggle="tab"><g:message
                    code="crmAccount.tab.tenants.label"/><crm:countIndicator
                    count="${tenantList.size()}"/></a>
            </li>
        </g:if>
        <crm:pluginViews location="tabs" var="view">
            <crm:pluginTab id="${view.id}" label="${view.label}" count="${view.model?.totalCount}"/>
        </crm:pluginViews>
    </ul>

    <div class="tab-content">

        <div class="tab-pane active" id="account">

            <f:with bean="${crmAccount}">

                <div class="row-fluid">

                    <div class="span4">
                        <div class="row-fluid">

                            <f:field property="name" input-class="span10" input-autofocus=""/>
                            <f:field property="address1" input-class="span10"/>
                            <!--
                            <f:field property="address2" input-class="span10"/>
                            <f:field property="address3" input-class="span10"/>
                            -->
                            <div class="control-group">
                                <label class="control-label"><g:message
                                        code="crmAccount.postalAddress.label"/></label>

                                <div class="controls">
                                    <g:textField name="postalCode" value="${crmAccount.postalCode}"
                                                 class="span3"/>
                                    <g:textField name="city" value="${crmAccount.city}"
                                                 class="span7"/>
                                </div>
                            </div>
                            <!--
                            <f:field property="region" input-class="span10"/>
                            -->
                            <f:field property="countryCode">
                                <g:countrySelect name="countryCode" value="${crmAccount.countryCode}"
                                                 noSelection="['': '']" class="span10"/>
                            </f:field>
                        </div>
                    </div>

                    <div class="span4">
                        <div class="row-fluid">
                            <f:field property="reference" input-class="span10"/>
                            <f:field property="email" input-class="span10"/>
                            <f:field property="telephone" input-class="span6"/>
                            <f:field property="ssn" input-class="span6"/>
                        </div>
                    </div>


                    <div class="span4">
                        <div class="row-fluid">

                            <h4>Resursutnyttjande</h4>

                            <table class="table table-bordered">
                                <thead>
                                <tr>
                                    <th>Resurs</th>
                                    <th>Utnyttjat</th>
                                    <th>Maxgräns</th>
                                </tr>
                                <tbody>
                                <tr>
                                    <td>Arkivplats</td>
                                    <td>${WebUtils.bytesFormatted(crmAccount.getOption('resourceUsage') ?: 0)}</td>
                                    <td>${crmAccount.getItem('crmContent')?.quantity ?: 0} GB</td>
                                </tr>
                                <tr>
                                    <td>Antal vyer</td>
                                    <td><a href="#tenants">${tenantList.size()} st</a></td>
                                    <td>${crmAccount.getItem('crmTenant')?.quantity ?: 1} st</td>
                                </tr>
                                <tr>
                                    <td>Antal administratörer</td>
                                    <td>${roles.admin?.size() ?: 0} st</td>
                                    <td>${crmAccount.getItem('crmAdmin')?.quantity ?: 1} st</td>
                                </tr>
                                <tr>
                                    <td>Antal användare</td>
                                    <td>${roles.user?.size() ?: 0} st</td>
                                    <td>${crmAccount.getItem('crmUser')?.quantity ?: 0} st</td>
                                </tr>
                                <tr>
                                    <td>Antal gäster</td>
                                    <td>${roles.guest?.size() ?: 0} st</td>
                                    <td>${crmAccount.getItem('crmGuest')?.quantity ?: 0} st</td>
                                </tr>
                                </tbody>
                                </thead>
                            </table>
                        </div>
                    </div>

                </div>
            </f:with>


            <div class="form-actions">
                <g:if test="${crmAccount.isActive()}">
                    <crm:button action="edit" visual="primary" icon="icon-ok icon-white"
                                label="crmAccount.button.update.label"/>
                </g:if>

                <g:unless test="${tenantList}">
                    <crm:isAllowedMoreTenants account="${crmAccount}">
                        <crm:button type="link" controller="crmTenant" action="create"
                                    params="${['account.id': crmAccount.id]}" visual="success"
                                    icon="icon-file icon-white"
                                    label="crmTenant.button.create.label"/>
                    </crm:isAllowedMoreTenants>
                </g:unless>

                <crm:button action="delete" visual="danger" label="crmAccount.button.delete.label"
                            confirm="crmAccount.delete.confirm.message"/>

                <g:unless test="${grailsApplication.config.crm.account.multiple && !crmAccount.isTrial()}">
                    <g:link action="create" title="${message(code: 'crmAccount.create.help')}"
                            style="margin-left:10px;">
                        <g:message code="crmAccount.create.label"/>
                    </g:link>
                </g:unless>
            </div>
        </div>

        <g:if test="${tenantList}">
            <div class="tab-pane" id="tenants">
                <div class="row-fluid">

                    <table class="table table-striped">
                        <thead>
                        <th><g:message code="crmTenant.name.label"/></th>
                        <th><g:message code="crmTenant.locale.label"/></th>
                        <th><g:message code="crmTenant.dateCreated.label"/></th>
                        </thead>
                        <tbody>
                        <g:each in="${tenantList}" var="tenant">
                            <tr>
                                <td>
                                    <g:link controller="crmTenant" action="edit"
                                            id="${tenant.id}">${tenant.name.encodeAsHTML()}</g:link>
                                </td>
                                <td>
                                    ${tenant.localeInstance.getDisplayName(request.locale ?: Locale.default)}
                                </td>

                                <td>
                                    <g:formatDate type="date" date="${tenant.dateCreated}"/>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>

                    <div class="form-actions">
                        <g:if test="${crmAccount.isActive()}">
                            <crm:isAllowedMoreTenants account="${crmAccount}">
                                <crm:button type="link" controller="crmTenant" action="create"
                                            params="${['account.id': crmAccount.id]}" visual="success"
                                            icon="icon-file icon-white"
                                            label="crmTenant.button.create.label"/>
                            </crm:isAllowedMoreTenants>
                        </g:if>

                    </div>
                </div>
            </div>
        </g:if>

        <crm:pluginViews location="tabs" var="view">
            <div class="tab-pane tab-${view.id}" id="${view.id}">
                <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
            </div>
        </crm:pluginViews>

    </div>
</div>

</g:form>

</body>
</html>
