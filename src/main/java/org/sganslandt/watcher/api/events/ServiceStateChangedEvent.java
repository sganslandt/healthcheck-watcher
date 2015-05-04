package org.sganslandt.watcher.api.events;

import lombok.Data;
import org.sganslandt.watcher.core.Service;

@Data
public class ServiceStateChangedEvent {
    private final String serviceName;
    private final Service.State state;
}
