<%@ page contentType="text/html;charset=UTF-8" %>
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

<g:form action="delete">
    <g:hiddenField name="id" value="${crmAccount.id}"/>


    <div class="alert alert-block alert-danger">
        <p><strong>Är du helt säker på att du vill radera abonnemanget?</strong></p>

        <p>Allt du registrerat kommer att raderas!</p>
        <g:if test="${crmAccount.tenants}">
            <p>Följande vyer kommer att raderas:</p>
            <ul>
                <g:each in="${crmAccount.tenants}" var="t">
                    <li>${t.name}</li>
                </g:each>
            </ul>
        </g:if>
    </div>

    <div class="form-actions">
        <crm:button visual="danger" action="delete" id="${crmAccount.id}" confirm="crmAccount.delete.confirm.message"
                    label="crmAccount.button.delete.label" icon="icon-trash icon-white"/>
        <crm:button type="link" icon="icon-remove" label="crmAccount.button.cancel.label"
                    action="index"/>
    </div>
</g:form>

</body>
</html>