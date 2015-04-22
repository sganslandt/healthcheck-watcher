package org.sganslandt.watcher;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.sganslandt.watcher.external.HealthChecker;
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
    public void run(Configuration configuration,
                    Environment environment) {
        final Client client = new JerseyClientBuilder(environment).using(configuration.getHttpClient())
                .build(getName());
        final HealthChecker component = new HealthChecker(client);
        environment.jersey().register(component);
        final HealthsResource resource = new HealthsResource(component);
        environment.jersey().register(resource);
    }

}