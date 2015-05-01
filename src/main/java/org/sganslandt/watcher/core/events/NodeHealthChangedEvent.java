package org.sganslandt.watcher.core.events;

import lombok.Data;
import org.sganslandt.watcher.core.Health;

import java.util.List;

@Data
public class NodeHealthChangedEvent {
    private final String serviceName;
    private final String serviceUrl;
    private final List<Health> healths;
}
