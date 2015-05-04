package org.sganslandt.watcher.api.events;

import lombok.Data;

@Data
public class ServiceRemovedEvent {
    private final String serviceName;
}
