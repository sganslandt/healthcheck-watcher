package org.sganslandt.watcher.core;

import lombok.Data;

@Data
public class System {
    private final State state;
    private final Iterable<Service> services;

    public enum State {
        Healthy, Unhealthy
    }

}
