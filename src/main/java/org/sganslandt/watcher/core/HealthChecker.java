package org.sganslandt.watcher.core;

import com.google.common.eventbus.EventBus;
import io.dropwizard.lifecycle.Managed;
import org.sganslandt.watcher.core.events.HealthChangedEvent;
import org.sganslandt.watcher.core.events.NodeRemovedEvent;
import org.sganslandt.watcher.core.events.ServiceAddedEvent;
import org.sganslandt.watcher.core.events.ServiceRemovedEvent;
import org.sganslandt.watcher.external.HealthCheckerClient;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HealthChecker implements Managed {

    // Dependencies
    private final HealthCheckerClient healthCheckerClient;
    private final ServiceDOA dao;
    private final EventBus eventBus;
    private final ScheduledThreadPoolExecutor scheduler;

    // Data
    private final Map<String, Map<String, Health>> nodeHealths;

    public HealthChecker(final HealthCheckerClient healthCheckerClient, final ServiceDOA dao, final EventBus eventBus) {
        this.healthCheckerClient = healthCheckerClient;
        this.dao = dao;
        this.eventBus = eventBus;
        this.nodeHealths = new ConcurrentHashMap<>();

        this.scheduler = new ScheduledThreadPoolExecutor(100);
        this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkAll();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Add a service to the registry. Signals that service is part of the system, but is
     * not yet deployed or we don't know where it is yet.
     *
     * @param serviceName Name of the service
     */
    public void addService(String serviceName) {
        dao.addService(serviceName);
        eventBus.post(new ServiceAddedEvent(serviceName));
    }

    /**
     * Start monitoring a node of a service.
     *
     * @param serviceName Name of the service
     * @param url         Root URL of the newly deployed version of the service
     */
    public void monitor(String serviceName, String url) {
        if (!dao.listServices().contains(serviceName))
            addService(serviceName);

        dao.addNode(serviceName, url);
    }

    /**
     * Stop monitoring a specific node of a service.
     *
     * @param serviceName Name of the service
     * @param url         Root URL of the node to stop monitoring
     */
    public void stopMonitoring(String serviceName, String url) {
        dao.removeNode(serviceName, url);
        nodeHealths.remove(url);
        eventBus.post(new NodeRemovedEvent(serviceName, url));
    }

    /**
     * Completely remove a service from the system.
     *
     * @param serviceName Name of the service
     */
    public void removeService(String serviceName) {
        for (String url : dao.listNodes(serviceName))
            stopMonitoring(serviceName, url);

        dao.removeAllNodes(serviceName);
        dao.removeService(serviceName);
        eventBus.post(new ServiceRemovedEvent(serviceName));
    }

    private void checkAll() {
        for (final String serviceName : dao.listServices()) {
            for (final String url : dao.listNodes(serviceName)) {
                scheduler.execute(new Runnable() {
                    @Override
                    public void run() {
                        check(serviceName, url);
                    }
                });
            }
        }
    }

    private void check(String serviceName, String url) {
        final Map<String, Health> nodeHealths = healthCheckerClient.check(url);
        if (!(this.nodeHealths.containsKey(url) && this.nodeHealths.get(url).equals(nodeHealths))) {
            eventBus.post(new HealthChangedEvent(serviceName, url, nodeHealths));
            this.nodeHealths.put(url, nodeHealths);
        }
    }

    @Override
    public void start() throws Exception {
        for (String serviceName : dao.listServices())
            eventBus.post(new ServiceAddedEvent(serviceName));
    }

    @Override
    public void stop() throws Exception {
    }
}
