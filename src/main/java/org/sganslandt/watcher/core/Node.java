package org.sganslandt.watcher.core;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public final class Node {
    private final String id;
    private final String url;
    private final State state;
    private final Role role;
    private final List<Health> healths;

    public Node (String url, State state, Role role, List<Health> healths) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.state = state;
        this.role = role;
        this.healths = healths;
    }

    public static enum Role {

        Active(true), Passive(false);

        private boolean active;

        private Role(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }
    }

    public static enum State {
        Healthy, Unhealthy, Unknown
    }
}
