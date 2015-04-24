package org.sganslandt.watcher.core;

import com.google.common.eventbus.EventBus;
import org.sganslandt.watcher.core.events.HealthChangedEvent;
import org.sganslandt.watcher.core.events.ServiceAddedEvent;
import org.sganslandt.watcher.core.events.ServiceRemovedEvent;
import org.sganslandt.watcher.external.HealthCheckerClient;
import org.sganslandt.watcher.external.HealthResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HealthChecker {

    private final Map<String, List<String>> servicesToWatch;
    private final HealthCheckerClient healthCheckerClient;
    private final EventBus eventBus;

    private final ScheduledThreadPoolExecutor scheduler;

    public HealthChecker(final HealthCheckerClient healthCheckerClient, final EventBus eventBus) {
        this.healthCheckerClient = healthCheckerClient;
        this.eventBus = eventBus;
        this.servicesToWatch = new ConcurrentHashMap<>();

        scheduler = new ScheduledThreadPoolExecutor(100);
        scheduler.scheduleAtFixedRate(new Runnable() {
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
        servicesToWatch.put(serviceName, new LinkedList<String>());
        eventBus.post(new ServiceAddedEvent(serviceName));
    }

    /**
     * Start monitoring an instance of a service.
     *
     * @param serviceName Name of the service
     * @param url         Root URL of the newly deployed version of the service
     */
    public void monitor(String serviceName, String url) {
        synchronized (servicesToWatch) {
            if (!servicesToWatch.containsKey(serviceName))
                addService(serviceName);

            servicesToWatch.get(serviceName).add(url);
        }
    }

    /**
     * Stop monitoring a specific instance of a service.
     *
     * @param serviceName Name of the service
     * @param url         Root URL of the instance to stop monitoring
     */
    public void stopMonitoring(String serviceName, String url) {
        if (!servicesToWatch.containsKey(serviceName))
            return;

        servicesToWatch.get(serviceName).remove(url);
    }

    /**
     * Completely remove a service from the system.
     *
     * @param serviceName Name of the service
     */
    public void removeService(String serviceName) {
        servicesToWatch.remove(serviceName);
        eventBus.post(new ServiceRemovedEvent(serviceName));
    }

    private void checkAll() {
        for (Map.Entry<String, List<String>> entry : servicesToWatch.entrySet()) {
            final String serviceName = entry.getKey();
            for (final String url : entry.getValue()) {
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
        final Map<String, HealthResult> healthResult = healthCheckerClient.check(url);
        eventBus.post(new HealthChangedEvent(serviceName, url, healthResult));
    }

}
