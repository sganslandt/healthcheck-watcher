package org.sganslandt.watcher.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.Subscribe;
import org.sganslandt.watcher.ViewSettings;
import org.sganslandt.watcher.core.*;
import org.sganslandt.watcher.core.System;
import org.sganslandt.watcher.core.events.NodeHealthChangedEvent;
import org.sganslandt.watcher.core.events.NodeRemovedEvent;
import org.sganslandt.watcher.core.events.ServiceAddedEvent;
import org.sganslandt.watcher.core.events.ServiceRemovedEvent;
import org.sganslandt.watcher.views.SystemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.util.Collections.sort;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class HealthsResource {

    private static final Logger log = LoggerFactory.getLogger(HealthsResource.class);

    private final HealthChecker healthChecker;
    private final ViewSettings viewSettings;

    private final Map<String, Map<String, List<Health>>> serviceHealths;

    public HealthsResource(final HealthChecker healthChecker, final ViewSettings viewSettings) {
        this.healthChecker = healthChecker;
        this.viewSettings = viewSettings;
        this.serviceHealths = new ConcurrentHashMap<>();
    }

    @Subscribe
    public void handle(ServiceAddedEvent event) {
        log.info("Received {}", event);
        serviceHealths.put(event.getServiceName(), new HashMap<String, List<Health>>());
    }

    @Subscribe
    public void handle(NodeHealthChangedEvent event) {
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
    @Produces(MediaType.TEXT_HTML)
    public SystemView getSystemHTML() {
        return new SystemView(
                new System(
                        resolveState(getServices()),
                        getServices()
                ), viewSettings);
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public org.sganslandt.watcher.core.System getSystemJSON() {
        return new System(
                resolveState(getServices()),
                getServices()
        );
    }

    private System.State resolveState(final List<Service> services) {
        for (Service s : services)
            if (!EnumSet.of(Service.State.Absent, Service.State.Healthy).contains(s.getState()))
                return System.State.Unhealthy;

        return System.State.Healthy;
    }

    @GET
    @Timed
    @Path("/service")
    public List<Service> getServices() {
        List<Service> services = new LinkedList<>();
        services.addAll(transform(newArrayList(serviceHealths.entrySet()), new Function<Map.Entry<String, Map<String, List<Health>>>, Service>() {
            @Override
            public Service apply(final Map.Entry<String, Map<String, List<Health>>> input) {
                final LinkedList<Node> serviceHealths = new LinkedList<>();
                serviceHealths.addAll(Collections2.transform(input.getValue().entrySet(), new Function<Map.Entry<String, List<Health>>, Node>() {
                    @Override
                    public Node apply(final Map.Entry<String, List<Health>> input) {
                        return new Node(input.getKey(), resolveState(input.getValue()), Node.Role.Active, input.getValue());
                    }
                }));
                return new Service(input.getKey(), resolveState(serviceHealths), serviceHealths);
            }

            private Service.State resolveState(final LinkedList<Node> serviceHealths) {
                if (serviceHealths.isEmpty())
                    return Service.State.Absent;
                else {
                    Collection<Node> activeNodes = filter(serviceHealths, new Predicate<Node>() {
                        @Override
                        public boolean apply(final Node input) {
                            return input.getRole() == Node.Role.Active;
                        }
                    });
                    if (activeNodes.isEmpty())
                        return Service.State.Absent;

                    for (Node node : activeNodes) {
                        for (Health health : node.getHealths()) {
                            if (!health.isHealthy())
                                return Service.State.Unhealthy;
                        }
                    }

                    return Service.State.Healthy;
                }
            }

            private Node.State resolveState(final List<Health> healths) {
                if (!healths.iterator().hasNext()) {
                    return Node.State.Unknown;
                } else {
                    for (Health h : healths)
                        if (!h.isHealthy())
                            return Node.State.Unhealthy;

                    return Node.State.Healthy;
                }
            }

        }));

        sort(services, byStateAndName());

        return services;
    }

    private static Comparator<Service> byStateAndName() {
        return new Comparator<Service>() {
            @Override
            public int compare(final Service o1, final Service o2) {
                if (o1.getState() == o2.getState())
                    return o1.getServiceName().compareTo(o2.getServiceName());

                return getStateOrder(o1.getState()).compareTo(getStateOrder(o2.getState()));
            }

            private Integer getStateOrder(final Service.State state) {
                switch (state) {
                    case Absent:
                        return 5;
                    case Healthy:
                        return 4;
                    case Unhealthy:
                        return 0;
                    default:
                        return -1;
                }
            }
        };
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