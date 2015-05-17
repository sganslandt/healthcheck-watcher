package org.sganslandt.watcher.core.hooks;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.sganslandt.watcher.api.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class NotifierHook {

    private static final Logger log = LoggerFactory.getLogger(NotifierHook.class);

    @Getter
    private final String url;
    private final Client client;

    private static final Map<Class, String> pathForClass = new HashMap<Class, String>() {{
        put(NodeAddedEvent.class, "/nodeAdded");
        put(NodeHealthChangedEvent.class, "/nodeHealthChanged");
        put(NodeRemovedEvent.class, "/nodeRemoved");
        put(ServiceAddedEvent.class, "/serviceAdded");
        put(ServiceRemovedEvent.class, "/serviceRemoved");
        put(ServiceStateChangedEvent.class, "/serviceStateChanged");
        put(SystemStateChangedEvent.class, "/systemStateChanged");
    }};

    public NotifierHook(final String url, final Client client) {
        this.url = url;
        this.client = client;
    }

    @Subscribe
    public void handle(final Object event) {
        doNotify(event);
    }

    private void doNotify(final Object event) {
        String path = pathForClass.get(event.getClass());
        if (path == null)
            return;

        Response.StatusType status = client.target(url)
                .path(path).request()
                .post(Entity.entity(event, MediaType.APPLICATION_JSON_TYPE))
                .getStatusInfo();
        if (status.getFamily() != Response.Status.Family.SUCCESSFUL)
            log.warn("Failed to notify {} about {}, {}", url, event, status.getStatusCode());
    }


}
