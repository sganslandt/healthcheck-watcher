package org.sganslandt.watcher.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.Subscribe;
import org.sganslandt.watcher.core.HealthChecker;
import org.sganslandt.watcher.core.events.HealthChangedEvent;
import org.sganslandt.watcher.core.events.ServiceAddedEvent;
import org.sganslandt.watcher.external.HealthResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Iterables.transform;

@Path("/healths")
@Produces(MediaType.APPLICATION_JSON)
public class HealthsResource {

    private static final Logger log = LoggerFactory.getLogger(HealthsResource.class);

    private final HealthChecker healthChecker;

    private final Map<String, Map<String, Map<String, HealthResult>>> serviceHealths;

    public HealthsResource(final HealthChecker healthChecker) {
        this.healthChecker = healthChecker;
        this.serviceHealths = new ConcurrentHashMap<>();
    }

    @Subscribe
    public void handle(ServiceAddedEvent event) {
        log.info("Received {}", event);
        serviceHealths.put(event.getServiceName(), new HashMap<String, Map<String, HealthResult>>());
    }

    @Subscribe
    public void handle(HealthChangedEvent event) {
        log.info("Received {}", event);
        serviceHealths.get(event.getServiceName()).put(event.getServiceUrl(), event.getHealths());
    }

    @GET
    @Timed
    public Iterable<ServiceHealths> healths() {
        return transform(serviceHealths.entrySet(), new Function<Map.Entry<String, Map<String, Map<String, HealthResult>>>, ServiceHealths>() {
            @Override
            public ServiceHealths apply(final Map.Entry<String, Map<String, Map<String, HealthResult>>> input) {
                final LinkedList<InstanceHealths> serviceHealths = new LinkedList<InstanceHealths>();
                serviceHealths.addAll(Collections2.transform(input.getValue().entrySet(), new Function<Map.Entry<String, Map<String, HealthResult>>, InstanceHealths>() {
                    @Override
                    public InstanceHealths apply(final Map.Entry<String, Map<String, HealthResult>> input) {
                        return new InstanceHealths(input.getKey(), input.getValue());
                    }
                }));
                return new ServiceHealths(input.getKey(), serviceHealths);
            }
        });
    }

    @PUT
    @Timed
    public void addServiceToWatch(@QueryParam("name") String name, @QueryParam("url") Optional<String> url) {
        log.info("Adding watch of {} at {}", name, url);
        if (url.isPresent())
            healthChecker.monitor(name, url.get());
        else
            healthChecker.addService(name);
    }

    @DELETE
    @Timed
    public void removeServiceFromWatchList(@QueryParam("name") String name, @QueryParam("url") Optional<String> url) {
        log.info("Remove watch of {} at {}", name, url);
        if (url.isPresent())
            healthChecker.stopMonitoring(name, url.get());
        else
            healthChecker.removeService(name);
    }

}