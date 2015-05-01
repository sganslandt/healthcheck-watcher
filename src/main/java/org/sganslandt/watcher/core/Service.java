package org.sganslandt.watcher.core;

import lombok.Data;

import java.util.List;

@Data
public final class Service {
    private final String serviceName;
    private final State state;
    private final List<Node> nodes;

    public enum State {
            Absent, Healthy, Unhealthy, Unknown
    }

}
