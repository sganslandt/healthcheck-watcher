package org.sganslandt.watcher.api.events;

import lombok.Data;
import org.sganslandt.watcher.core.health.Health;
import org.sganslandt.watcher.core.health.Node;

import java.util.List;

@Data
public class NodeHealthChangedEvent {
    public final String serviceName;
    private final String nodeUrl;
    private final Node.State state;
    private final List<Health> healths;
}
