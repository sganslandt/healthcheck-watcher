package org.sganslandt.watcher.core;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import org.sganslandt.watcher.api.events.NodeHealthChangedEvent;
import org.sganslandt.watcher.external.HealthCheckerClient;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

public final class Node {

    public static final int INITIAL_CHECK_DELAY = 10;

    private final String serviceName;
    @Getter(AccessLevel.PACKAGE)
    private final String url;
    @Getter(AccessLevel.PACKAGE)
    private Role role;
    private List<Health> healths;

    private final ScheduledThreadPoolExecutor scheduler;
    private final HealthCheckerClient healthCheckerClient;
    private final EventBus eventBus;

    public Node(String serviceName, String url, Role role, final HealthCheckerClient healthCheckerClient, final int checkInterval, final EventBus eventBus) {
        this.eventBus = eventBus;
        this.serviceName = serviceName;
        this.url = url;
        this.role = role;
        this.healths = new LinkedList<>();

        this.healthCheckerClient = healthCheckerClient;
        this.scheduler = new ScheduledThreadPoolExecutor(1);
        this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkHealth();
            }
        }, INITIAL_CHECK_DELAY, checkInterval, TimeUnit.MILLISECONDS);
    }

    private void checkHealth() {
        List<Health> nodeHealths = transform(newArrayList(healthCheckerClient.check(url).entrySet()), new Function<Map.Entry<String, org.sganslandt.watcher.external.Health>, Health>() {
            @Nullable
            @Override
            public Health apply(final Map.Entry<String, org.sganslandt.watcher.external.Health> input) {
                return new Health(input.getKey(), input.getValue().isHealthy(), input.getValue().getMessage());
            }
        });

        if (!healths.containsAll(nodeHealths))
            eventBus.post(new NodeHealthChangedEvent(serviceName, url, resolveState(nodeHealths), nodeHealths));
    }

    void stop() {
        scheduler.shutdown();
    }

    private State resolveState(final List<Health> healths) {
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
        if (event.getNodeUrl().equals(url)) {
            healths.clear();
            healths.addAll(event.getHealths());
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
