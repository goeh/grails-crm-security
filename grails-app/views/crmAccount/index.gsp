<%@ page import="grails.plugins.crm.security.CrmTenant; grails.plugins.crm.core.TenantUtils" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="crmAccount.index.title" args="[crmAccount.name]" default="Subscription"/></title>
</head>

<body>

<header class="page-header clearfix">
    <h1 class="pull-left">
        <g:message code="crmAccount.index.title" default="Subscription" args="[crmAccount.name]"/>
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

<g:form action="index">

    <div class="tabbable">

        <ul class="nav nav-tabs">
            <li class="active"><a href="#account" data-toggle="tab"><g:message code="crmAccount.tab.account.label"/></a>
            </li>
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

                                <f:field property="name" input-class="span10"/>
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

                                <table class="table tabled-bordered">
                                    <thead>
                                    <tr>
                                        <th>Resurs</th>
                                        <th>Utnyttjande</th>
                                        <th>Maxgräns</th>
                                    </tr>
                                    <tbody>
                                    <tr>
                                        <td>Arkivplats</td>
                                        <td>n/a</td>
                                        <td>${crmAccount.getOption('maxQuota') ?: 0} GB</td>
                                    </tr>
                                    <tr>
                                        <td>Antal vyer</td>
                                        <td>${crmAccount.tenants.size()} st</td>
                                        <td>${crmAccount.getOption('maxTenants') ?: 0} st</td>
                                    </tr>
                                    <tr>
                                        <td>Antal administratörer</td>
                                        <td>${roles.admin?.size() ?: 0}</td>
                                        <td>${crmAccount.getOption('maxAdmins') ?: 0}</td>
                                    </tr>
                                    <tr>
                                        <td>Antal användare</td>
                                        <td>${roles.user?.size() ?: 0}</td>
                                        <td>${crmAccount.getOption('maxUsers') ?: 0}</td>
                                    </tr>
                                    <tr>
                                        <td>Antal gäster</td>
                                        <td>${roles.guest?.size() ?: 0}</td>
                                        <td>${crmAccount.getOption('maxGuests') ?: 0}</td>
                                    </tr>
                                    </tbody>
                                    </thead>
                                </table>

                            </div>
                        </div>

                    </div>
                </f:with>


                <div class="form-actions">
                    <crm:button visual="primary" icon="icon-ok icon-white"
                                label="crmAccount.button.update.label"/>
                    <crm:button type="link" mapping="crm-account-store" visual="success"
                                icon="icon-shopping-cart icon-white"
                                label="crmAccount.button.features.label"/>
                </div>
            </div>

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
