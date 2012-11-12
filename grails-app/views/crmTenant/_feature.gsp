<div class="thumbnail">

    <div class="feature-body">
        <h5 class="center">
            ${message(code: 'feature.' + f.name + '.label', default: f.name)}
            <span class="statistics" data-crm-feature="${f.name}"><span class="badge">?</span></span>
        </h5>

        <p>${message(code: 'feature.' + f.name + '.description', default: f.description)}</p>
    </div>

    <g:set var="readme" value="${false}"/>

    <div class="form-actions" style="margin-bottom: 0;">

        <div class="btn-group">
            <g:if test="${installed.contains(f.name)}">
                <a href="javascript:void(0)" class="btn btn-primary btn-small">
                    Aktiverad
                </a>
            </g:if>
            <g:else>
                <g:link controller="crmFeature" action="install" params="${[id: crmTenant.id, name: f.name]}"
                        class="btn btn-small">
                    Aktivera
                </g:link>
            </g:else>

            <button class="btn ${installed.contains(f.name) ? 'btn-primary' : ''} btn-small dropdown-toggle"
                    data-toggle="dropdown">
                <span class="caret"></span>
            </button>

            <ul class="dropdown-menu">
                <g:if test="${readme}">
                    <a href="${crm.createResourceLink(resource: readme)}" class="crm-readme">Läs mer...</a>
                </g:if>
                <g:if test="${installed.contains(f.name) && !f.required}">
                    <crm:hasPermission permission="crmFeature:install:${crmTenant.id}">
                        <g:link controller="crmFeature" action="uninstall" params="${[id: crmTenant.id, name: f.name]}"
                                onclick="return confirm('Är du säker på att du vill avaktivera funktionen ${f.name}?')">
                            Avaktivera
                        </g:link>
                    </crm:hasPermission>
                </g:if>
                <g:if test="${!installed.contains(f.name)}">
                    <g:link controller="crmFeature" action="install" params="${[id: crmTenant.id, name: f.name]}">
                        Aktivera
                    </g:link>
                </g:if>
            </ul>

        </div>

    </div>

</div>