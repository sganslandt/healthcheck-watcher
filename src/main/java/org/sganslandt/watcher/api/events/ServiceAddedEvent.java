package org.sganslandt.watcher.api.events;

import lombok.Data;

@Data
public class ServiceAddedEvent {
    private final String serviceName;
}
