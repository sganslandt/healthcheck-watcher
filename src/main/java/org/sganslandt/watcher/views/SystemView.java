package org.sganslandt.watcher.views;

import io.dropwizard.views.View;
import org.sganslandt.watcher.ViewSettings;

public class SystemView extends View {
    private final org.sganslandt.watcher.core.System system;
    private final ViewSettings viewSettings;

    public SystemView(final org.sganslandt.watcher.core.System system, ViewSettings viewSettings) {
        super("system.ftl");
        this.system = system;
        this.viewSettings = viewSettings;
    }

    public org.sganslandt.watcher.core.System getSystem() {
        return system;
    }

    public ViewSettings getSettings() {
        return viewSettings;
    }

}
