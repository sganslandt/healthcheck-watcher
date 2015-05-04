package org.sganslandt.watcher.api.events;

import lombok.Data;

@Data
public class NodeRemovedEvent {
    private final String serviceName;
    private final String url;
}
