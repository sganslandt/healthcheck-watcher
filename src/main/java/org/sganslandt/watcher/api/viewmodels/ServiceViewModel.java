package org.sganslandt.watcher.api.viewmodels;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ServiceViewModel {
    @Getter
    private final String serviceName;
    @Getter
    @Setter
    private String state;
    @Getter
    private List<NodeViewModel> nodes;

    ServiceViewModel(final String serviceName) {
        this.serviceName = serviceName;
        this.state = "Absent";
        this.nodes = new LinkedList<>();
    }

    void addNode(final String nodeUrl) {
        nodes.add(new NodeViewModel(nodeUrl));
    }

    void removeNode(final String nodeUrl) {
        Iterator<NodeViewModel> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            NodeViewModel node = iterator.next();
            if (node.getUrl().equals(nodeUrl))
                iterator.remove();
        }
    }

    NodeViewModel getNode(final String nodeUrl) {
        for (NodeViewModel node : nodes)
            if (node.getUrl().equals(nodeUrl))
                return node;

        return null;
    }
}
