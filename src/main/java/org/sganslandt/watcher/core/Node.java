package org.sganslandt.watcher.core;

import com.google.common.eventbus.Subscribe;
import lombok.Data;
import lombok.Getter;
import org.sganslandt.watcher.core.events.NodeHealthChangedEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public final class Node {
    @Getter
    private final String id;
    @Getter
    private final String url;
    @Getter
    private final Role role;
    @Getter
    private final List<Health> healths;

    public Node(String url, Role role) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.role = role;
        this.healths = new LinkedList<>();
    }

    public State getState() {
        if (!healths.iterator().hasNext()) {
            return Node.State.Unknown;
        } else {
            for (Health h : healths)
                if (!h.isHealthy())
                    return Node.State.Unhealthy;

            return Node.State.Healthy;
        }
    }

    @Subscribe
    public void handle(final NodeHealthChangedEvent event) {
        if (event.getServiceUrl().equals(url)) {
            synchronized (healths) {
                healths.clear();
                healths.addAll(event.getHealths());
            }
        }
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
