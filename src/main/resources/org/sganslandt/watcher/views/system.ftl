<!DOCTYPE html>
<html lang="en">
<title>watcher</title>
<head>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>

    <meta http-equiv="refresh" content="5">
</head>
<body>

<h2>
<#if system.state = "Healthy"><span class="label label-success">${system.state}</span></#if>
<#if system.state != "Healthy"><span class="label label-danger">${system.state}</span></#if>
</h2>

<ul>
<#list system.services as service>
    <li>
        <h3>
            <#if service.state = "Healthy"><span class="label label-success">${service.serviceName}</span></#if>
            <#if service.state != "Healthy"><span class="label label-danger">${service.serviceName}</span></#if>
        </h3>

        <ul>
            <#list service.nodes as node>
                <li>
                    <h4>
                        <#if node.state = "Healthy"><span class="label label-success">${node.url} (${node.role})</span></#if>
                        <#if node.state != "Healthy"><span class="label label-danger">${node.url} (${node.role})</span></#if>
                    </h4>
                    <ul>
                        <#list node.healths as health>
                            <li>
                                <#assign value>
                                ${health.name}<#if health.message??>: ${health.message}</#if>
                                </#assign>
                                <#if health.healthy><span class="label label-success">${value}</span></#if>
                                <#if !health.healthy><span class="label label-danger">${value}</span></#if>
                            </li>
                        </#list>
                    </ul>
                </li>
            </#list>
        </ul>
    </li>
</#list>
</ul>
</body>
</html>