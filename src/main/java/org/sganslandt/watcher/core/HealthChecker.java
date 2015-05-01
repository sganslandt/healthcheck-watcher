package org.sganslandt.watcher.core;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;
import io.dropwizard.lifecycle.Managed;
import org.sganslandt.watcher.core.events.*;
import org.sganslandt.watcher.external.HealthCheckerClient;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

public class HealthChecker implements Managed {

    // Dependencies
    private final HealthCheckerClient healthCheckerClient;
    private final ServiceDAO dao;
    private final EventBus eventBus;
    private final ScheduledThreadPoolExecutor scheduler;

    // Data
    private final Map<String, Iterable<Health>> nodeHealths;

    public HealthChecker(final HealthCheckerClient healthCheckerClient, final ServiceDAO dao, final EventBus eventBus, int checkInterval) {
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
        }, 5, checkInterval, TimeUnit.SECONDS);
    }

    /**
     * Add a service to the registry. Signals that service is part of the system, but is
     * not yet deployed or we don't know where it is yet.
     *
     * @param serviceName Name of the service
     */
    public void addService(String serviceName) {
        if (dao.listServices().contains(serviceName))
            return;

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
        if (!dao.listServices().contains(serviceName)) {
            addService(serviceName);
            doAddNode(serviceName, url);
        } else if (!dao.listNodes(serviceName).contains(url)) {
            doAddNode(serviceName, url);
        }
    }

    private void doAddNode(final String serviceName, final String url) {
        eventBus.post(new NodeAddedEvent(serviceName, url));
        dao.addNode(serviceName, url);
    }

    /**
     * Stop monitoring a specific node of a service.
     *
     * @param serviceName Name of the service
     * @param url         Root URL of the node to stop monitoring
     */
    public void stopMonitoring(String serviceName, String url) {
        if (!dao.listNodes(serviceName).contains(url))
            return;

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

        if (dao.listServices().contains(serviceName)) {
            dao.removeService(serviceName);
            eventBus.post(new ServiceRemovedEvent(serviceName));
        }
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
        List<Health> nodeHealths = transform(newArrayList(healthCheckerClient.check(url).entrySet()), new Function<Map.Entry<String, org.sganslandt.watcher.external.Health>, Health>() {
            @Nullable
            @Override
            public Health apply(final Map.Entry<String, org.sganslandt.watcher.external.Health> input) {
                return new Health(input.getKey(), input.getValue().isHealthy(), input.getValue().getMessage());
            }
        });

        if (!(this.nodeHealths.containsKey(url) && this.nodeHealths.get(url).equals(nodeHealths))) {
            eventBus.post(new NodeHealthChangedEvent(serviceName, url, nodeHealths));
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
