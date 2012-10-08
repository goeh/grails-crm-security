<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="crmSecurityAdmin.title" default="Security Administration"/></title>
</head>

<body>

<div class="row-fluid">

    <div class="span9">

        <h1>User Account Statistics</h1>

        <table class="table table-bordered">
            <thead>
            <tr>
                <th>Counter</th>
                <th>Active</th>
                <th>Total</th>
            </tr>
            </thead>

            <tbody>
            <tr>
                <td>Users</td>
                <td>${user.active}</td>
                <td>${user.total}</td>
            </tr>
            <tr>
                <td>Tenants</td>
                <td>${tenant.active}</td>
                <td>${tenant.total}</td>
            </tr>
            </tbody>
        </table>

    </div>

    <div class="span3">
    </div>
</div>

</body>
</html>
