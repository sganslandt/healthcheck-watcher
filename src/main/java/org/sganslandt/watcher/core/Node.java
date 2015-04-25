package org.sganslandt.watcher.core;

import lombok.Data;

import java.util.Map;

@Data
public final class Node {
    private final String url;
    private final State state;
    private final Role role;
    private final Map<String, Health> healths;

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
        Starting, Running, Stopping, Unknown
    }
}
