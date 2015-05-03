package org.sganslandt.watcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.eventbus.EventBus;
import lombok.Data;
import org.sganslandt.watcher.core.HealthChecker;
import org.sganslandt.watcher.core.ServiceDAO;
import org.sganslandt.watcher.external.HealthCheckerClient;

@Data
public class HealthCheckerFactory {
    // TODO This doesn't belong here, but this will soon be SystemFactory...
    private final String systemName;
    private final int checkInterval;

    @JsonCreator
    public HealthCheckerFactory(@JsonProperty("name") final String systemName, @JsonProperty("checkInterval") final int checkInterval) {
        this.systemName = systemName;
        this.checkInterval = checkInterval;
    }

    public HealthChecker build(final HealthCheckerClient healthCheckerClient, final ServiceDAO dao, final EventBus eventBus) {
        return new HealthChecker(healthCheckerClient, dao, eventBus, checkInterval);
    }
}
