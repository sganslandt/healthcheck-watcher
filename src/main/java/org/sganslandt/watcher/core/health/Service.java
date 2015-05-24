package org.sganslandt.watcher.core.health;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import org.sganslandt.watcher.api.events.NodeAddedEvent;
import org.sganslandt.watcher.api.events.NodeHealthChangedEvent;
import org.sganslandt.watcher.api.events.NodeRemovedEvent;
import org.sganslandt.watcher.api.events.ServiceStateChangedEvent;
import org.sganslandt.watcher.external.HealthCheckerClient;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

public final class Service {
    @Getter(AccessLevel.PACKAGE)
    private final String serviceName;
    private List<Node> nodes;
    private Map<String, Node.State> nodeStates;
    private State state;

    private final HealthCheckerClient healthCheckerClient;
    private final int checkInterval;
    private final EventBus eventBus;

    public Service(String serviceName, final HealthCheckerClient healthCheckerClient, final int checkInterval, EventBus eventBus) {
        this.serviceName = serviceName;
        this.healthCheckerClient = healthCheckerClient;
        this.checkInterval = checkInterval;
        this.state = State.Unknown;
        this.nodes = new LinkedList<>();
        this.nodeStates = new HashMap<>();
        this.eventBus = eventBus;
    }

    private State resolveState() {
        if (nodes.isEmpty())
            return Service.State.Absent;
        else {
            Collection<Node> activeNodes = filter(nodes, new Predicate<Node>() {
                @Override
                public boolean apply(final Node input) {
                    return input.getRole() == Node.Role.Active;
                }
            });
            if (activeNodes.isEmpty())
                return Service.State.Absent;

            for (Node node : activeNodes) {
                if (nodeStates.get(node.getUrl()) != Node.State.Healthy)
                    return Service.State.Unhealthy;
            }

            return Service.State.Healthy;
        }
    }

    public void monitor(final String url) {
        if (transform(nodes, toURL()).contains(url))
            return;

        eventBus.post(new NodeAddedEvent(serviceName, url));
    }

    private void updateServiceState() {
        State newState = resolveState();
        if (newState != state)
            eventBus.post(new ServiceStateChangedEvent(serviceName, newState));
    }

    public void stopMonitoring() {
        List<Node> allNodes = new LinkedList<>();
        allNodes.addAll(nodes);
        for (Node node : allNodes)
            eventBus.post(new NodeRemovedEvent(serviceName, node.getUrl()));
    }

    public void stopMonitoring(final String url) {
        if (!transform(nodes, toURL()).contains(url))
            return;

        eventBus.post(new NodeRemovedEvent(serviceName, url));
    }

    @Subscribe
    public void handle(final NodeAddedEvent event) {
        if (event.getServiceName().equals(serviceName)) {
            Node node = new Node(serviceName, event.getNodeUrl(), Node.Role.Active, healthCheckerClient, checkInterval, eventBus);
            nodes.add(node);
            nodeStates.put(event.getNodeUrl(), Node.State.Unknown);
            eventBus.register(node);
            updateServiceState();
        }
    }

    @Subscribe
    public void handle(final NodeHealthChangedEvent event) {
        if (event.getServiceName().equals(serviceName)) {
            nodeStates.put(event.getNodeUrl(), event.getState());
            updateServiceState();
        }
    }

    @Subscribe
    public void handle(final ServiceStateChangedEvent event) {
        if (event.getServiceName().equals(serviceName))
            state = event.getState();
    }

    @Subscribe
    public void handle(final NodeRemovedEvent event) {
        nodeStates.remove(event.getUrl());
        Iterator<Node> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (event.getServiceName().equals(serviceName) && node.getUrl().equals(event.getUrl())) {
                node.stop();
                iterator.remove();
                eventBus.unregister(node);
                updateServiceState();
            }
        }
    }

    public enum State {
        Absent, Healthy, Unhealthy, Unknown
    }

    private Function<Node, String> toURL() {
        return new Function<Node, String>() {
            @Nullable
            @Override
            public String apply(final Node input) {
                return input.getUrl();
            }
        };
    }

}
