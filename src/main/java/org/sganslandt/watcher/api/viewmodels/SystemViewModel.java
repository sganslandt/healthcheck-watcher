package org.sganslandt.watcher.api.viewmodels;

import com.google.common.eventbus.Subscribe;
import jersey.repackaged.com.google.common.base.Function;
import lombok.Getter;
import org.sganslandt.watcher.api.events.*;
import org.sganslandt.watcher.core.Health;
import org.sganslandt.watcher.core.Service;

import javax.annotation.Nullable;
import java.util.*;

import static jersey.repackaged.com.google.common.collect.Lists.transform;

public class SystemViewModel {
    @Getter
    private final String systemName;
    @Getter
    private String state;
    private List<ServiceViewModel> services;

    public SystemViewModel(final String systemName) {
        this.systemName = systemName;
        this.state = "Healthy";
        this.services = new LinkedList<>();
    }

    public List<ServiceViewModel> getServices() {
        return services;
    }

    @Subscribe
    public void handle(final SystemStateChangedEvent event) {
        state = event.getState().toString();
        Collections.sort(services, byStateAndName());
    }

    @Subscribe
    public void handle(final ServiceAddedEvent event) {
        services.add(new ServiceViewModel(event.getServiceName()));
        Collections.sort(services, byStateAndName());
    }

    @Subscribe
    public void handle(final ServiceStateChangedEvent event) {
        getService(event.getServiceName()).setState(event.getState().toString());
        Collections.sort(services, byStateAndName());
    }

    @Subscribe
    public void handle(final ServiceRemovedEvent event) {
        Iterator<ServiceViewModel> iterator = services.iterator();
        while (iterator.hasNext()) {
            ServiceViewModel service = iterator.next();
            if (service.getServiceName().equals(event.getServiceName()))
                iterator.remove();
        }
        Collections.sort(services, byStateAndName());
    }

    @Subscribe
    public void handle(final NodeAddedEvent event) {
        ServiceViewModel service = getService(event.getServiceName());
        service.addNode(event.getNodeUrl());
    }

    @Subscribe
    public void handle(final NodeHealthChangedEvent event) {
        getService(event.getServiceName()).getNode(event.getNodeUrl()).setState(event.getState().toString(), transform(event.getHealths(), toViewModel()));
    }

    @Subscribe
    public void handle(final NodeRemovedEvent event) {
        ServiceViewModel service = getService(event.getServiceName());
        service.removeNode(event.getUrl());
    }


    private ServiceViewModel getService(final String serviceName) {
        for (ServiceViewModel service : services)
            if (service.getServiceName().equals(serviceName))
                return service;

        return null;
    }

    private Function<Health, HealthViewModel> toViewModel() {
        return new Function<Health, HealthViewModel>() {
            @Nullable
            @Override
            public HealthViewModel apply(final Health health) {
                return new HealthViewModel(health.getName(), health.isHealthy(), health.getMessage());
            }
        };
    }

    private Comparator<ServiceViewModel> byStateAndName() {
        return new Comparator<ServiceViewModel>() {
            @Override
            public int compare(final ServiceViewModel o1, final ServiceViewModel o2) {

                if (o1.getState().equals(o2.getState()))
                    return o1.getServiceName().compareTo(o2.getServiceName());

                return getStateOrder(o1.getState()).compareTo(getStateOrder(o2.getState()));
            }

            private Integer getStateOrder(final String state) {
                switch (state) {
                    case "Absent":
                        return 5;
                    case "Healthy":
                        return 4;
                    case "Unhealthy":
                        return 0;
                    default:
                        return -1;
                }
            }
        };
    }
}
