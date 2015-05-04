package org.sganslandt.watcher.core;

import com.google.common.eventbus.Subscribe;
import org.sganslandt.watcher.api.events.NodeAddedEvent;
import org.sganslandt.watcher.api.events.NodeRemovedEvent;
import org.sganslandt.watcher.api.events.ServiceAddedEvent;
import org.sganslandt.watcher.api.events.ServiceRemovedEvent;

public class TableUpdater {

    private final ServiceDAO dao;

    public TableUpdater(final ServiceDAO dao) {
        this.dao = dao;
    }

    @Subscribe
    public void handle(final ServiceAddedEvent event) {
        dao.addService(event.getServiceName());
    }

    @Subscribe
    public void handle(final ServiceRemovedEvent event) {
        dao.removeAllNodes(event.getServiceName());
        dao.removeService(event.getServiceName());
    }

    @Subscribe
    public void handle(final NodeAddedEvent event) {
        dao.addNode(event.getServiceName(), event.getNodeUrl());
    }

    @Subscribe
    public void handle(final NodeRemovedEvent event) {
        dao.removeNode(event.getServiceName(), event.getUrl());
    }

}
