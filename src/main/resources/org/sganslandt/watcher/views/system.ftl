<!DOCTYPE html>
<html lang="en">
<title>watcher</title>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">

    <meta http-equiv="refresh" content="${settings.refreshInterval}">
</head>
<body>

<div class="jumbotron">
    <div class="container">
        <h1>watcher of ${system.systemName}</h1>

        <p>Shows a neat summary of the services deployed, topology and health of the system.</p>

        <h2>
        <#if system.state = "Healthy"><span class="label label-success">${system.state}</span></#if>
        <#if system.state != "Healthy"><span class="label label-danger">${system.state}</span></#if>
        </h2>
    </div>
</div>

<div class="container-fluid">
    <div class="row">
    <#list system.services as service>
        <div class="col-md-3">
            <#assign panelClass>
                <#if service.state = "Healthy">panel-success</#if>
                <#if service.state = "Absent">panel-warning</#if>
                <#if service.state != "Healthy" && service.state != "Absent">panel-danger</#if>
            </#assign>
            <div class="panel ${panelClass}">

                <div class="panel-heading">
                    <h3 class="panel-title">${service.serviceName}</h3>
                </div>

                <div class="panel-body">
                    <ul class="list-group">
                        <#list service.nodes as node>
                            <li class="list-group-item">
                                <h4>
                                    <#if node.state = "Healthy">
                                        <button class="btn btn-primary btn-success" data-toggle="collapse"
                                                data-target="#${node.id}" aria-expanded="false"
                                                aria-controls="${node.id}">
                                        ${node.url} (${node.role})
                                        </button>
                                        <#assign collapse>collapse</#assign>
                                    </#if>
                                    <#if node.state != "Healthy">
                                        <button class="btn btn-primary btn-danger" data-toggle="collapse"
                                                data-target="#${node.id}" aria-expanded="false"
                                                aria-controls="${node.id}">
                                        ${node.url} (${node.role})
                                        </button>
                                        <#assign collapse>collapse.in</#assign>
                                    </#if>
                                </h4>

                                <div class="${collapse}" id="${node.id}">
                                    <ul class="list-group">
                                        <#list node.healths as health>
                                            <#assign value>
                                            ${health.name}<#if health.message??>: ${health.message}</#if>
                                            </#assign>

                                            <#if health.healthy>
                                                <li class="list-group-item list-group-item-success">${value}</li>
                                            </#if>
                                            <#if !health.healthy>
                                                <li class="list-group-item list-group-item-danger">${value}</li>
                                            </#if>
                                        </#list>
                                    </ul>
                                </div>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div>
        </div>
    </#list>
    </div>
</div>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
</body>
</html>