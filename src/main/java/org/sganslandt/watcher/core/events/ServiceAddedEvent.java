package org.sganslandt.watcher.core.events;

import lombok.Data;

@Data
public class ServiceAddedEvent {
    private final String serviceName;
}
