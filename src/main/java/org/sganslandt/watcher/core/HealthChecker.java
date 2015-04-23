package org.sganslandt.watcher.core;

import com.google.common.eventbus.EventBus;
import org.sganslandt.watcher.external.HealthCheckerClient;
import org.sganslandt.watcher.external.HealthResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HealthChecker {

    private final Map<String, List<String>> servicesToWatch;
    private final HealthCheckerClient healthCheckerClient;
    private final EventBus eventBus;

    public HealthChecker(final HealthCheckerClient healthCheckerClient, final EventBus eventBus) {
        this.healthCheckerClient = healthCheckerClient;
        this.eventBus = eventBus;
        this.servicesToWatch = new HashMap<>();

        final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkAll();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void checkAll() {
        for (Map.Entry<String, List<String>> entry : servicesToWatch.entrySet()) {
            final String serviceName = entry.getKey();
            for (String url : entry.getValue()) {
                final Map<String, HealthResult> healthResult = healthCheckerClient.check(url);
                eventBus.post(new HealthChangedEvent(serviceName, url, healthResult));
            }
        }
    }

    public void monitor(String serviceName, String url) {
        synchronized (servicesToWatch) {
            if (!servicesToWatch.containsKey(serviceName))
                servicesToWatch.put(serviceName, new LinkedList<String>());

            servicesToWatch.get(serviceName).add(url);
        }
    }

    public void stopMonitoring(String serviceName, String url) {
        if (!servicesToWatch.containsKey(serviceName))
            return;

        servicesToWatch.get(serviceName).remove(url);
    }

    public void removeService(String serviceName) {
        servicesToWatch.remove(serviceName);
    }

}
