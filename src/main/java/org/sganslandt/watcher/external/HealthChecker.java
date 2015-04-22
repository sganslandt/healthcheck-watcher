package org.sganslandt.watcher.external;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.Map;

public class HealthChecker {

    private final Client client;

    public HealthChecker(final Client client) {
        this.client = client;
    }

    public Map<String, HealthResult> check(String url) {
        return client
                .target(url).path("/health")
                .request().get()
                .readEntity(new GenericType<Map<String, HealthResult>>(Map.class));
    }

}
