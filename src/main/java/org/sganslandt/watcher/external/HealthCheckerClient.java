package org.sganslandt.watcher.external;

import java.util.Map;

public interface HealthCheckerClient {
    /**
     * Fetch the healths of node.
     *
     * @param url Root url of the Node.
     * @return All the health checks of the node, or a generic unhealthy "connection" health check if the node
     * is not available.
     */
    Map<String, Health> check(String url);
}
