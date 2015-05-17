package org.sganslandt.watcher.resources;

import com.codahale.metrics.annotation.Timed;
import org.sganslandt.watcher.core.hooks.HooksManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/notifierHooks")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HooksResource {

    private static final Logger log = LoggerFactory.getLogger(HooksResource.class);
    private final HooksManager hooksManager;

    public HooksResource(final HooksManager hooksManager) {
        this.hooksManager = hooksManager;
    }

    @PUT
    @Timed
    public void addHook(@QueryParam("url") String url) {
        if (hooksManager.listHooks().contains(url)) {
            log.info("{} already registered as a hook", url);
        }
        else {
            log.info("Adding a notifier hook at {}", url);
            hooksManager.addHook(url);
        }
    }

    @GET
    @Timed
    public Collection<String> listHooks() {
        return hooksManager.listHooks();
    }

    @DELETE
    @Timed
    public void deleteHook(@QueryParam("url") String url) {
        log.info("Deleting notifier hook at {}", url);
        hooksManager.removeHook(url);
    }

}
