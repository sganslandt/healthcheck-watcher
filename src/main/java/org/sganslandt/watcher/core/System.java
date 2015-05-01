package org.sganslandt.watcher.core;

import lombok.Data;

import java.util.List;

@Data
public class System {
    private final State state;
    private final List<Service> services;

    public enum State {
        Healthy, Unhealthy
    }

}
