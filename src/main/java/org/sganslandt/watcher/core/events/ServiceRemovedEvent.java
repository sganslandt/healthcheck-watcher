package org.sganslandt.watcher.core.events;

import lombok.Data;

@Data
public class ServiceRemovedEvent {
    private final String serviceName;
}
