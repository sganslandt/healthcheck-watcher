package org.sganslandt.watcher.views;

import io.dropwizard.views.View;

public class SystemView extends View {
    private final org.sganslandt.watcher.core.System system;

    public SystemView(final org.sganslandt.watcher.core.System system) {
        super("system.ftl");
        this.system = system;
    }

    public org.sganslandt.watcher.core.System getSystem() {
        return system;
    }
}
