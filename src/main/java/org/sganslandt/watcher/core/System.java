package org.sganslandt.watcher.core;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.sganslandt.watcher.core.events.ServiceAddedEvent;
import org.sganslandt.watcher.core.events.ServiceRemovedEvent;
import org.sganslandt.watcher.external.HealthCheckerClient;

import java.util.*;

import static com.google.common.collect.Collections2.transform;

public class System {
    private final String systemName;
    private final HealthCheckerClient healthCheckerClient;
    private final List<Service> services;
    private final EventBus eventBus;

    public System(final String systemName, final HealthCheckerClient healthCheckerClient, final EventBus eventBus) {
        this.systemName = systemName;
        this.healthCheckerClient = healthCheckerClient;
        this.services = new LinkedList<>();
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

    public State getState() {
        for (Service s : services)
            if (!EnumSet.of(Service.State.Absent, Service.State.Healthy).contains(s.getState()))
                return System.State.Unhealthy;

        return System.State.Healthy;
    }

    public String getSystemName() {
        return systemName;
    }

    public List<Service> getServices() {
        List<Service> services = new LinkedList<>();
        services.addAll(this.services);
        Collections.sort(services, byStateAndName());
        return services;
    }

    public enum State {
        Healthy, Unhealthy
    }

    @Subscribe
    public void handle(ServiceAddedEvent event) {
        Service service = new Service(event.getServiceName(), healthCheckerClient, eventBus);
        services.add(service);
        eventBus.register(service);
    }

    @Subscribe
    public void handle(ServiceRemovedEvent event) {
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            if (service.getServiceName().equals(event.getServiceName())) {
                iterator.remove();
                eventBus.unregister(service);
            }
        }
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

    private Function<Node, String> toURL() {
        return new Function<Node, String>() {
            @Override
            public String apply(final Node input) {
                return input.getUrl();
            }
        };
    }

    private static Comparator<Service> byStateAndName() {
        return new Comparator<Service>() {
            @Override
            public int compare(final Service o1, final Service o2) {
                if (o1.getState() == o2.getState())
                    return o1.getServiceName().compareTo(o2.getServiceName());

                return getStateOrder(o1.getState()).compareTo(getStateOrder(o2.getState()));
            }

            private Integer getStateOrder(final Service.State state) {
                switch (state) {
                    case Absent:
                        return 5;
                    case Healthy:
                        return 4;
                    case Unhealthy:
                        return 0;
                    default:
                        return -1;
                }
            }
        };
    }

}
