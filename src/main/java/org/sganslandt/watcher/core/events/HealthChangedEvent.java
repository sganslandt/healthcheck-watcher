package org.sganslandt.watcher.core.events;

import lombok.Data;
import org.sganslandt.watcher.external.HealthResult;

import java.util.Map;

@Data
public class HealthChangedEvent {
    private final String serviceName;
    private final String serviceUrl;
    private final Map<String, HealthResult> healths;
}
