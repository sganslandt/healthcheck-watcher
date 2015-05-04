package org.sganslandt.watcher.core;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.sganslandt.watcher.api.events.ServiceAddedEvent;
import org.sganslandt.watcher.api.events.ServiceRemovedEvent;
import org.sganslandt.watcher.api.events.ServiceStateChangedEvent;
import org.sganslandt.watcher.api.events.SystemStateChangedEvent;
import org.sganslandt.watcher.external.HealthCheckerClient;

import java.util.*;

import static com.google.common.collect.Collections2.transform;

public class System {
    private final String systemName;
    private final HealthCheckerClient healthCheckerClient;
    private final int checkInterval;
    private final List<Service> services;
    private final Map<String, Service.State> serviceStates;
    private final EventBus eventBus;

    public System(final String systemName, final HealthCheckerClient healthCheckerClient, final int checkInterval, final EventBus eventBus) {
        this.systemName = systemName;
        this.healthCheckerClient = healthCheckerClient;
        this.checkInterval = checkInterval;
        this.services = new LinkedList<>();
        this.serviceStates = new HashMap<>();
        this.eventBus = eventBus;
    }

    /**
     * Add a service to the registry. Signals that service is part of the system, but is
     * not yet deployed or we don't know where it is yet.
     *
     * @param serviceName Name of the service
     */
    public void addService(String serviceName) {
        if (transform(services, toServiceName()).contains(serviceName))
            return;

        eventBus.post(new ServiceAddedEvent(serviceName));
    }

    /**
     * Start monitoring a node of a service.
     *
     * @param serviceName Name of the service
     * @param url         Root URL of the newly deployed version of the service
     */
    public void monitor(String serviceName, String url) {
        if (!transform(services, toServiceName()).contains(serviceName))
            addService(serviceName);

        getService(serviceName).monitor(url);
    }

    /**
     * Stop monitoring a specific node of a service.
     *
     * @param serviceName Name of the service
     * @param url         Root URL of the node to stop monitoring
     */

    public void stopMonitoring(String serviceName, String url) {
        if (!transform(services, toServiceName()).contains(serviceName))
            return;

        getService(serviceName).stopMonitoring(url);
    }

    /**
     * Completely remove a service from the system.
     *
     * @param serviceName Name of the service
     */
    public void removeService(String serviceName) {
        if (!transform(services, toServiceName()).contains(serviceName))
            return;

        getService(serviceName).stopMonitoring();
        eventBus.post(new ServiceRemovedEvent(serviceName));
    }

    private State resolveState() {
        for (Service s : services)
            if (!EnumSet.of(Service.State.Absent, Service.State.Healthy).contains(serviceStates.get(s.getServiceName())))
                return System.State.Unhealthy;

        return System.State.Healthy;
    }

    public enum State {
        Healthy, Unhealthy
    }

    @Subscribe
    public void handle(final ServiceAddedEvent event) {
        Service service = new Service(event.getServiceName(), healthCheckerClient, checkInterval, eventBus);
        services.add(service);
        serviceStates.put(event.getServiceName(), Service.State.Absent);
        eventBus.register(service);
        eventBus.post(new SystemStateChangedEvent(systemName, resolveState()));
    }

    @Subscribe
    public void handle(final ServiceStateChangedEvent event) {
        serviceStates.put(event.getServiceName(), event.getState());
        eventBus.post(new SystemStateChangedEvent(systemName, resolveState()));
    }

    @Subscribe
    public void handle(final ServiceRemovedEvent event) {
        serviceStates.remove(event.getServiceName());
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            if (service.getServiceName().equals(event.getServiceName())) {
                iterator.remove();
                eventBus.unregister(service);
            }
        }
        eventBus.post(new SystemStateChangedEvent(systemName, resolveState()));
    }

    private Service getService(String serviceName) {
        for (Service service : services)
            if (service.getServiceName().equals(serviceName))
                return service;

        throw new IllegalArgumentException("No such service " + serviceName);
    }

    private static Function<Service, String> toServiceName() {
        return new Function<Service, String>() {
            @Override
            public String apply(final Service input) {
                return input.getServiceName();
            }
        };
    }



}
