package org.sganslandt.watcher.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class JerseyHealthCheckerClient implements HealthCheckerClient {

    private static final Logger log = getLogger(JerseyHealthCheckerClient.class);

    private final Client client;

    public JerseyHealthCheckerClient(final Client client) {
        this.client = client;
    }

    @Override
    public Map<String, Health> check(String url) {
        try {
            log.debug("Checking health of {}", url);
            HashMap<String, Health> healths = client
                    .target(url).path("/healthcheck")
                    .request().get()
                    .readEntity(new GenericType<HashMap<String, Health>>() {
                    });
            log.debug("Received healths {}: {}", url, healths);
            return healths;
        } catch (ProcessingException e) {
            log.debug("Failed to read the health of " + url, e);
            return healthFromThrowable(e);
        } catch (Throwable t) {
            log.warn("Failed to read the health.", t);
            return healthFromThrowable(t);
        }
    }

    private HashMap<String, Health> healthFromThrowable(final Throwable e) {
        final HashMap<String, Health> result = new HashMap<>();
        result.put("connection", new Health(false, e.getMessage()));
        return result;
    }

}
