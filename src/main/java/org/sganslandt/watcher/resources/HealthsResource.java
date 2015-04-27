package org.sganslandt.watcher.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.Subscribe;
import org.sganslandt.watcher.core.*;
import org.sganslandt.watcher.core.System;
import org.sganslandt.watcher.core.events.HealthChangedEvent;
import org.sganslandt.watcher.core.events.NodeRemovedEvent;
import org.sganslandt.watcher.core.events.ServiceAddedEvent;
import org.sganslandt.watcher.core.events.ServiceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Iterables.transform;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class HealthsResource {

    private static final Logger log = LoggerFactory.getLogger(HealthsResource.class);

    private final HealthChecker healthChecker;

    private final Map<String, Map<String, Map<String, Health>>> serviceHealths;

    public HealthsResource(final HealthChecker healthChecker) {
        this.healthChecker = healthChecker;
        this.serviceHealths = new ConcurrentHashMap<>();
    }

    @Subscribe
    public void handle(ServiceAddedEvent event) {
        log.info("Received {}", event);
        serviceHealths.put(event.getServiceName(), new HashMap<String, Map<String, Health>>());
    }

    @Subscribe
    public void handle(HealthChangedEvent event) {
        log.info("Received {}", event);
        serviceHealths.get(event.getServiceName()).put(event.getServiceUrl(), event.getHealths());
    }

    @Subscribe
    public void handle(NodeRemovedEvent event) {
        serviceHealths.get(event.getServiceName()).remove(event.getUrl());
    }

    @Subscribe
    public void handle(ServiceRemovedEvent event) {
        serviceHealths.remove(event.getServiceName());
    }

    @GET
    @Timed
    @Path("/")
    public org.sganslandt.watcher.core.System getSystem() {
        return new System(
                resolveState(getServices()),
                getServices()
        );
    }

    private System.State resolveState(final Iterable<Service> services) {
        for (Service s : services)
            if (!EnumSet.of(Service.State.Absent, Service.State.Healthy).contains(s.getState()))
                return System.State.Unhealthy;

        return System.State.Healthy;
    }

    @GET
    @Timed
    @Path("/service")
    public Iterable<Service> getServices() {
        return transform(serviceHealths.entrySet(), new Function<Map.Entry<String, Map<String, Map<String, Health>>>, Service>() {
            @Override
            public Service apply(final Map.Entry<String, Map<String, Map<String, Health>>> input) {
                final LinkedList<Node> serviceHealths = new LinkedList<>();
                serviceHealths.addAll(Collections2.transform(input.getValue().entrySet(), new Function<Map.Entry<String, Map<String, Health>>, Node>() {
                    @Override
                    public Node apply(final Map.Entry<String, Map<String, Health>> input) {
                        return new Node(input.getKey(), resolveState(input.getValue().values()), Node.Role.Active, input.getValue());
                    }
                }));
                return new Service(input.getKey(), resolveState(serviceHealths), serviceHealths);
            }

            private Node.State resolveState(final Collection<Health> healths) {
                if (healths.isEmpty()) {
                    return Node.State.Unknown;
                } else {
                    for (Health h : healths)
                        if (!h.isHealthy())
                            return Node.State.Unhealthy;

                    return Node.State.Healthy;
                }
            }

            private Service.State resolveState(final LinkedList<Node> serviceHealths) {
                if (serviceHealths.isEmpty())
                    return Service.State.Absent;
                else {
                    final Optional<Node> active = getActive(serviceHealths);
                    if (!active.isPresent())
                        return Service.State.Absent;

                    final Node activeNode = active.get();
                    for (Health health : activeNode.getHealths().values()) {
                        if (!health.isHealthy())
                            return Service.State.Unhealthy;
                    }

                    return Service.State.Healthy;
                }
            }

            private Optional<Node> getActive(final LinkedList<Node> nodes) {
                for (Node i : nodes)
                    if (i.getRole().isActive())
                        return Optional.of(i);
                return Optional.absent();
            }
        });
    }

    @PUT
    @Timed
    @Path("/service")
    public void addServiceToWatch(@QueryParam("name") String name, @QueryParam("url") Optional<String> url) {
        log.info("Adding watch of {} at {}", name, url);
        if (url.isPresent())
            healthChecker.monitor(name, url.get());
        else
            healthChecker.addService(name);
    }

    @DELETE
    @Timed
    @Path("/service")
    public void removeServiceFromWatchList(@QueryParam("name") String name, @QueryParam("url") Optional<String> url) {
        log.info("Remove watch of {} at {}", name, url);
        if (url.isPresent())
            healthChecker.stopMonitoring(name, url.get());
        else
            healthChecker.removeService(name);
    }

}