package org.sganslandt.watcher.external;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.HashMap;
import java.util.Map;

public class HealthCheckerClient {

    private final Client client;

    public HealthCheckerClient(final Client client) {
        this.client = client;
    }

    public Map<String, HealthResult> check(String url) {
        try {
            return client
                    .target(url).path("/health")
                    .request().get()
                    .readEntity(new GenericType<Map<String, HealthResult>>(Map.class));
        } catch (ProcessingException e) {
            final HashMap<String, HealthResult> result = new HashMap<>();
            result.put("connection", new HealthResult(false, e.getMessage()));
            return result;
        }
    }

}
