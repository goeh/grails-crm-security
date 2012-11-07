<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>

<g:set var="today" value="${new Date()}"/>

<table class="table table-striped">
    <thead>
    <tr>

        <th style="width:16px;"></th>
        <th><g:message code="crmTenant.name.label"/></th>
        <th><g:message code="crmTenant.account.label"/></th>
        <th><g:message code="crmTenant.locale.label"/></th>
        <th><g:message code="crmTenant.dateCreated.label"/></th>
        <th style="width:16px;"></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" var="m">
        <tr class="${m.current ? 'active' : ''}">

            <td style="width:16px;">
                <g:if test="${m.id == user.defaultTenant}"><i class="icon-home"></i></g:if>
            </td>

            <td>
                <g:if test="${m.user == crmUser}">
                    <g:link action="edit" id="${m.id}">${m.name}</g:link>
                </g:if>
                <g:else>
                    ${m.name}
                </g:else>
            </td>

            <td>
                ${m.account}
            </td>

            <td>
                ${m.localeInstance.getDisplayName(request.locale ?: Locale.default)}
            </td>

            <td>
                <g:formatDate type="date" date="${m.dateCreated}"/>
            </td>
<%--
            <td>
                <g:formatDate type="date" date="${m.expires}"/>
                <g:if test="${m.expires}">
                    <g:if test="${m.expires >= today}">
                        (<g:message code="default.days.left.message" args="${[m.expires - today]}"
                                    default="{0} days left"/>)
                    </g:if>
                    <g:else>
                        <span class="label label-important"><g:message code="crmTenant.expires.expired"
                                                                       default="Closed"/></span>
                    </g:else>
                </g:if>
                <g:else>
                    <g:message code="crmTenant.expires.never" default="Never"/>
                </g:else>
            </td>
--%>
            <td>
                <g:unless test="${m.current}">
                    <g:link action="activate" params="${[id: m.id, referer: createLink(action: 'index')]}"
                            class="btn btn-primary btn-small">
                        <g:message code="crmTenant.activate.label" default="Switch"/>
                    </g:link>
                </g:unless>

            </td>

        </tr>
    </g:each>
    </tbody>
</table>
