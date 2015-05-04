package org.sganslandt.watcher.views;

import io.dropwizard.views.View;
import org.sganslandt.watcher.ViewSettings;
import org.sganslandt.watcher.api.viewmodels.SystemViewModel;

public class SystemView extends View {

    private final SystemViewModel systemViewModel;
    private final ViewSettings viewSettings;

    public SystemView(final SystemViewModel systemViewModel, ViewSettings viewSettings) {
        super("system.ftl");
        this.viewSettings = viewSettings;
        this.systemViewModel = systemViewModel;
    }

    public SystemViewModel getSystem() {
        return systemViewModel;
    }

    public ViewSettings getSettings() {
        return viewSettings;
    }

}
