package org.sganslandt.watcher.resources;

import org.sganslandt.watcher.external.HealthResult;
import lombok.Data;

import java.util.Map;

@Data
final class Healths {
    private final String serviceName;
    private final String serviceUrl;
    private final Map<String, HealthResult> healths;
}
