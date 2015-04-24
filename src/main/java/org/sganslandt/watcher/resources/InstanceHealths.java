package org.sganslandt.watcher.resources;

import lombok.Data;
import org.sganslandt.watcher.external.HealthResult;

import java.util.Map;

@Data
class InstanceHealths {
    private final String url;
    private final Map<String, HealthResult> healths;
}
