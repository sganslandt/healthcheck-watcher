package org.sganslandt.watcher.api.events;

import lombok.Data;

@Data
public class SystemStateChangedEvent {
    private final String systemName;
    private final org.sganslandt.watcher.core.health.System.State state;
}
