package org.sganslandt.watcher.api.viewmodels;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class NodeViewModel {
    @Getter
    private final String id;
    @Getter
    private final String url;
    @Getter
    private final String role;
    @Getter
    private String state;
    @Getter
    private List<HealthViewModel> healths;

    NodeViewModel(final String url) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.state = "Booting...";
        this.role = "Active";
        this.healths = new LinkedList<>();
    }

    public void setState(final String state, final List<HealthViewModel> healths) {
        this.state = state;
        this.healths = healths;
    }
}
