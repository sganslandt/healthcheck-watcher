package org.sganslandt.watcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.eventbus.EventBus;
import lombok.Data;
import org.sganslandt.watcher.external.HealthCheckerClient;

@Data
public class SystemFactory {
    private final String systemName;
    private final int checkInterval;

    @JsonCreator
    public SystemFactory(@JsonProperty("name") final String systemName, @JsonProperty("checkInterval") final int checkInterval) {
        this.systemName = systemName;
        this.checkInterval = checkInterval;
    }

    public org.sganslandt.watcher.core.System build(final HealthCheckerClient healthCheckerClient, final EventBus eventBus) {
        return new org.sganslandt.watcher.core.System(systemName, healthCheckerClient, checkInterval, eventBus);
    }
}
