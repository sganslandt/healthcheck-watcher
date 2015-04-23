package org.sganslandt.watcher.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import lombok.Data;
import org.sganslandt.watcher.core.HealthChangedEvent;
import org.sganslandt.watcher.core.HealthChecker;
import org.sganslandt.watcher.external.HealthResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Iterables.transform;

@Path("/healths")
@Produces(MediaType.APPLICATION_JSON)
public class HealthsResource {

    private static final Logger log = LoggerFactory.getLogger(HealthsResource.class);

    private final HealthChecker healthChecker;

    private final Map<HealthKey, Map<String, HealthResult>> serviceHealths;

    public HealthsResource(final HealthChecker healthChecker) {
        this.healthChecker = healthChecker;
        this.serviceHealths = new ConcurrentHashMap<>();
    }

    @Subscribe
    public void handle(HealthChangedEvent event) {
        log.info("Received {}", event);
        serviceHealths.put(new HealthKey(event.getServiceName(), event.getServiceUrl()), event.getHealths());
    }

    @GET
    @Timed
    public Iterable<Healths> healths() {
        return transform(serviceHealths.entrySet(), new Function<Map.Entry<HealthKey, Map<String, HealthResult>>, Healths>() {
            @Override
            public Healths apply(final Map.Entry<HealthKey, Map<String, HealthResult>> input) {
                final HealthKey key = input.getKey();
                return new Healths(key.getServiceName(), key.getUrl(), input.getValue());
            }
        });
    }

    @PUT
    @Timed
    public void addServiceToWatch(@QueryParam("name") String name, @QueryParam("url") String url) {
        log.info("Adding watch of {} at {}", name, url);
        healthChecker.monitor(name, url);
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

    @Data
    private static class HealthKey {
        private final String serviceName;
        private final String url;
    }
}