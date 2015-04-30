package org.sganslandt.watcher.core.events;

import lombok.Data;
import org.sganslandt.watcher.core.Health;

import java.util.Map;

@Data
public class NodeAddedEvent {
    private final String serviceName;
    private final String serviceUrl;
}
