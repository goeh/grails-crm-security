<%@ page import="grails.plugins.crm.core.WebUtils; grails.plugins.crm.security.CrmTenant; grails.plugins.crm.core.TenantUtils" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmAccount.label', default: 'Subscription')}"/>
    <title><g:message code="crmAccount.edit.title" args="[entityName, crmAccount.name]"
                      default="Edit Subscription"/></title>
    <r:require modules="datepicker"/>
    <r:script>
        $(document).ready(function () {

            $('.date').datepicker({weekStart: 1});
        });
    </r:script>
</head>

<body>

<header class="page-header clearfix">
    <h1 class="pull-left">
        <g:message code="crmAccount.edit.title" default="Edit Subscription" args="[entityName, crmAccount.name]"/>
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

<g:form action="edit" id="${crmAccount.id}">
    <g:hiddenField name="version" value="${crmAccount.version}"/>

    <f:with bean="${crmAccount}">

        <div class="row-fluid">

            <div class="span6">
                <div class="row-fluid">
                    <div class="alert alert-success">
                        <h3>Faktureringsadress</h3>

                        <p>
                            ${fieldValue(bean: crmAccount, field: 'name')}<br/>
                            <g:if test="${crmAccount.reference}">
                                ${fieldValue(bean: crmAccount, field: 'reference')}<br/>
                            </g:if>
                            <g:if test="${crmAccount.address1}">
                                ${fieldValue(bean: crmAccount, field: 'address1')}<br/>
                            </g:if>
                            ${fieldValue(bean: crmAccount, field: 'postalCode')} ${fieldValue(bean: crmAccount, field: 'city')}<br/>
                            E-post ${fieldValue(bean: crmAccount, field: 'email')}<br/>
                            <g:if test="${crmAccount.telephone}">
                                Tel: ${fieldValue(bean: crmAccount, field: 'telephone')}<br/>
                            </g:if>
                            <g:if test="${crmAccount.ssn}">
                                Org.nr: ${fieldValue(bean: crmAccount, field: 'ssn')}
                            </g:if>
                        </p>
                    </div>

                    <f:field property="name" input-class="input-large"/>
                    <f:field property="email" input-class="input-large"/>

                    <f:field property="expires">
                        <div class="input-append date"
                             data-date="${formatDate(format: 'yyyy-MM-dd', date: crmAccount.expires ?: new Date())}">
                            <g:textField name="expires" class="input-medium" size="10"
                                         placeholder="ÅÅÅÅ-MM-DD"
                                         value="${formatDate(format: 'yyyy-MM-dd', date: crmAccount.expires)}"/><span
                                class="add-on"><i class="icon-th"></i></span>
                        </div>
                    </f:field>

                    <g:if test="${transfers}">
                        <div class="control-group">
                            <label class="control-label">Vyer att flytta till denna abonnemang</label>

                            <div class="controls">
                                <g:select from="${transfers}" name="transfer" optionKey="id" class="input-large"
                                          optionValue="${{ it.name + ' - ' + it.account.name + ' - ' + it.account.user.email }}"
                                          noSelection="['': '']"/>
                            </div>
                        </div>
                    </g:if>

                </div>
            </div>


            <div class="span6">
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
                            <td>${WebUtils.bytesFormatted(crmAccount.getOption('resourceUsage') ?: 0)}</td>
                            <td><input type="number" name="crmContent"
                                       value="${crmAccount.getItem('crmContent')?.quantity ?: 0}"
                                       class="input-small" min="0" max="25" step="1"/> GB</td>
                        </tr>
                        <tr>
                            <td>Antal vyer</td>
                            <td>${crmAccount.tenants.size()} st</td>
                            <td><input type="number" name="crmTenant"
                                       value="${crmAccount.getItem('crmTenant')?.quantity ?: 1}"
                                       class="input-small" min="1" max="999" step="1"/> st</td>
                        </tr>
                        <tr>
                            <td>Antal administratörer</td>
                            <td>${roles.admin?.size() ?: 0} st</td>
                            <td><input type="number" name="crmAdmin"
                                       value="${crmAccount.getItem('crmAdmin')?.quantity ?: 1}"
                                       class="input-small" min="1" max="999" step="1"/> st
                            </td>
                        </tr>
                        <tr>
                            <td>Antal användare</td>
                            <td>${roles.user?.size() ?: 0} st</td>
                            <td><input type="number" name="crmUser"
                                       value="${crmAccount.getItem('crmUser')?.quantity ?: 0}"
                                       class="input-small" min="0" max="999" step="1"/> st
                            </td>
                        </tr>
                        <tr>
                            <td>Antal gäster</td>
                            <td>${roles.guest?.size() ?: 0} st</td>
                            <td><input type="number" name="crmGuest"
                                       value="${crmAccount.getItem('crmGuest')?.quantity ?: 0}"
                                       class="input-small" min="0" max="999" step="1"/> st
                            </td>
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
                    label="crmAccount.button.save.label"/>

        <crm:hasPermission permission="crmAccount:delete:${crmAccount.id}">
            <g:link action="delete" id="${crmAccount.id}" style="color:#990000; margin-left:15px;"
                    onclick="return confirm('${message(code: 'crmAccount.delete.confirm.message', default: 'Are you really sure you want to delete your account?')}')">
                <g:message code="crmAccount.button.delete.label" default="Delete Account"/>
            </g:link>
        </crm:hasPermission>
    </div>

</g:form>

</body>
</html>
