package org.sganslandt.watcher.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.HashMap;
import java.util.Map;

public class JerseyHealthCheckerClient implements HealthCheckerClient {

    private static final Logger log = LoggerFactory.getLogger(JerseyHealthCheckerClient.class);

    private final Client client;

    public JerseyHealthCheckerClient(final Client client) {
        this.client = client;
    }

    @Override
    public Map<String, Health> check(String url) {
        try {
            return client
                    .target(url).path("/healthcheck")
                    .request().get()
                    .readEntity(new GenericType<HashMap<String, Health>>() {});
        } catch (ProcessingException e) {
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
