package org.sganslandt.watcher.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.sganslandt.watcher.core.events.NodeAddedEvent;
import org.sganslandt.watcher.core.events.NodeRemovedEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

public final class Service {
    @Getter
    private final String serviceName;
    @Getter
    private final List<Node> nodes;
    private final EventBus eventBus;

    public Service(String serviceName, EventBus eventBus) {
        this.serviceName = serviceName;
        this.nodes = new LinkedList<>();
        this.eventBus = eventBus;
    }

    public State getState() {
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
                if (node.getState() != Node.State.Healthy)
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
            Node node = new Node(event.getNodeUrl(), Node.Role.Active);
            nodes.add(node);
            eventBus.register(node);
        }
    }

    @Subscribe
    public void handle(final NodeRemovedEvent event) {
        Iterator<Node> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (event.getServiceName().equals(serviceName) && node.getUrl().equals(event.getUrl())) {
                iterator.remove();
                eventBus.unregister(node);
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
