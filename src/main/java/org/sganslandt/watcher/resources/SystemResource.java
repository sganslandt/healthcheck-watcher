package org.sganslandt.watcher.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import org.sganslandt.watcher.ViewSettings;
import org.sganslandt.watcher.api.viewmodels.ServiceViewModel;
import org.sganslandt.watcher.api.viewmodels.SystemViewModel;
import org.sganslandt.watcher.views.SystemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {

    private static final Logger log = LoggerFactory.getLogger(SystemResource.class);

    private final org.sganslandt.watcher.core.System system;
    private final SystemViewModel systemViewModel;
    private final ViewSettings viewSettings;

    public SystemResource(final org.sganslandt.watcher.core.System system, final SystemViewModel systemViewModel, final ViewSettings viewSettings) {
        this.system = system;
        this.systemViewModel = systemViewModel;
        this.viewSettings = viewSettings;
    }

    @GET
    @Timed
    @Produces(MediaType.TEXT_HTML)
    public SystemView getSystemHTML() {
        return new SystemView(systemViewModel, viewSettings);
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public SystemViewModel getSystemJSON() {
        return systemViewModel;
    }

    @GET
    @Timed
    @Path("/service")
    public List<ServiceViewModel> getServices() {
        return systemViewModel.getServices();
    }

    @PUT
    @Timed
    @Path("/service")
    public void addServiceToWatch(@QueryParam("name") String name, @QueryParam("url") Optional<String> url) {
        log.info("Adding watch of {} at {}", name, url);
        if (url.isPresent())
            system.monitor(name, url.get());
        else
            system.addService(name);
    }

    @DELETE
    @Timed
    @Path("/service")
    public void removeServiceFromWatchList(@QueryParam("name") String name, @QueryParam("url") Optional<String> url) {
        log.info("Remove watch of {} at {}", name, url);
        if (url.isPresent())
            system.stopMonitoring(name, url.get());
        else
            system.removeService(name);
    }

}