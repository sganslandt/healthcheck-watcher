package org.sganslandt.watcher.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.sganslandt.watcher.core.Healths;
import org.sganslandt.watcher.external.HealthChecker;
import org.sganslandt.watcher.external.HealthResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

@Path("/healths")
@Produces(MediaType.APPLICATION_JSON)
public class HealthsResource {

    private static final Logger log = LoggerFactory.getLogger(HealthsResource.class);

    private final HealthChecker healthChecker;
    private final Map<String, List<String>> servicesToWatch;

    public HealthsResource(final HealthChecker healthChecker) {
        this.healthChecker = healthChecker;
        this.servicesToWatch = new HashMap<>();
    }

    @GET
    @Timed
    public Iterable<Healths> healths() {
        return concat(transform(servicesToWatch.entrySet(), new Function<Map.Entry<String, List<String>>, List<Healths>>() {
            @Override
            public List<Healths> apply(final Map.Entry<String, List<String>> entry) {
                final String serviceName = entry.getKey();
                final List<Healths> result = new LinkedList<>();
                for (String url : entry.getValue()) {
                    final Map<String, HealthResult> healthResult = healthChecker.check(url);
                    result.add(new Healths(serviceName, url, healthResult));
                }

                return result;
            }
        }));
    }

    @PUT
    @Timed
    public void addServiceToWatch(@QueryParam("name") String name, @QueryParam("url") String url) {
        log.info("Adding watch of {} at {}", name, url);

        synchronized (servicesToWatch) {
            if (!servicesToWatch.containsKey(name))
                servicesToWatch.put(name, new LinkedList<String>());

            servicesToWatch.get(name).add(url);
        }
    }

    @DELETE
    @Timed
    public void removeServiceFromWatchList(@QueryParam("name") String name, @QueryParam("url") Optional<String> url) {
        log.info("Remove watch of {} at {}", name, url);

        if (!servicesToWatch.containsKey(name))
            return;

        synchronized (servicesToWatch) {
            if (url.isPresent())
                servicesToWatch.get(name).remove(url.get());
            else
                servicesToWatch.remove(name);
        }
    }

}