package org.sganslandt.watcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Configuration extends io.dropwizard.Configuration {

    private static final boolean DEFAULT_ASYNC_EVENTBUS = false;
    private static final int DEFAULT_EVENTBUS_THREADS = 0;
    private static final int DEFAULT_HEALTHCHECK_INTERVAL = 5;

    @Valid
    @NotNull
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    private EventBusFactory eventBus;

    @Valid
    @NotNull
    private SystemFactory systemFactory;

    @Valid
    @NotNull
    private ViewSettings viewSettings;

    @JsonProperty("httpClient")
    public JerseyClientConfiguration getHttpClient() {
        return httpClient;
    }

    @JsonProperty("httpClient")
    public void setHttpClient(JerseyClientConfiguration httpClient) {
        this.httpClient = httpClient;
    }

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }

    @JsonProperty("eventBus")
    public EventBusFactory getEventBus() {
        if (eventBus == null)
            return new EventBusFactory(DEFAULT_ASYNC_EVENTBUS, DEFAULT_EVENTBUS_THREADS);

        return eventBus;
    }

    @JsonProperty
    public void setEventBus(EventBusFactory eventBus) {
        this.eventBus = eventBus;
    }

    @JsonProperty
    public SystemFactory getSystem() {
        if (systemFactory == null)
            return new SystemFactory("unknown", DEFAULT_HEALTHCHECK_INTERVAL);
        return systemFactory;
    }

    @JsonProperty
    public void setSystem(final SystemFactory systemFactory) {
        this.systemFactory = systemFactory;
    }

    @JsonProperty
    public ViewSettings getViewSettings() {
        return viewSettings;
    }

    @JsonProperty
    public void setViewSettings(ViewSettings viewSettings) {
        this.viewSettings = viewSettings;
    }
}