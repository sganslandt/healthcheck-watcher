package org.sganslandt.watcher;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Configuration extends io.dropwizard.Configuration {
    @Valid
    @NotNull
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    private EventBusFactory eventBus;

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
        return eventBus;
    }

    @JsonProperty
    public void setEventBus(EventBusFactory eventBus) {
        this.eventBus = eventBus;
    }
}