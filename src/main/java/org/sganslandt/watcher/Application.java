package org.sganslandt.watcher;

import com.google.common.eventbus.EventBus;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.sganslandt.watcher.core.HealthChecker;
import org.sganslandt.watcher.external.HealthCheckerClient;
import org.sganslandt.watcher.resources.HealthsResource;

import javax.ws.rs.client.Client;

public class Application extends io.dropwizard.Application<Configuration> {
    public static void main(String[] args) throws Exception {
        new Application().run(args);
    }

    @Override
    public String getName() {
        return "healthcheck-watcher";
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
        final Client client =
                new JerseyClientBuilder(environment)
                        .using(configuration.getHttpClient())
                        .build(getName());
        final EventBus eventBus = new EventBus();

        final HealthCheckerClient healthCheckerClient = new HealthCheckerClient(client);
        final HealthChecker healthChecker = new HealthChecker(healthCheckerClient, eventBus);
        final HealthsResource resource = new HealthsResource(healthChecker);

        eventBus.register(resource);

        environment.jersey().register(healthCheckerClient);
        environment.jersey().register(resource);
    }

}