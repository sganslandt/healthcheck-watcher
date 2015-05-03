package org.sganslandt.watcher.core.events;

import lombok.Data;

@Data
public class NodeAddedEvent {
    private final String serviceName;
    private final String nodeUrl;
}
