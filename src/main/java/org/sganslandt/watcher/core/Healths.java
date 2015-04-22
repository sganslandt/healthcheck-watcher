package org.sganslandt.watcher.core;

import org.sganslandt.watcher.external.HealthResult;
import lombok.Data;

import java.util.Map;

@Data
public class Healths {
    private final String serviceName;
    private final String serviceUrl;
    private final Map<String, HealthResult> healths;
}
