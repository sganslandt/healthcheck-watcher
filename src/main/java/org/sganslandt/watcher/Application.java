package org.sganslandt.watcher;

import com.google.common.eventbus.EventBus;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.sganslandt.watcher.core.HealthChecker;
import org.sganslandt.watcher.core.ServiceDOA;
import org.sganslandt.watcher.external.HealthCheckerClient;
import org.sganslandt.watcher.resources.HealthsResource;
import org.skife.jdbi.v2.DBI;

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

        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "servicesDataSource");
        final ServiceDOA dao = jdbi.onDemand(ServiceDOA.class);
        dao.createServicesTable();
        dao.createURLsTable();

        final HealthCheckerClient healthCheckerClient = new HealthCheckerClient(client);
        final HealthChecker healthChecker = new HealthChecker(healthCheckerClient, dao, eventBus);
        final HealthsResource resource = new HealthsResource(healthChecker);

        environment.lifecycle().manage(healthChecker);

        eventBus.register(resource);

        environment.jersey().register(resource);
    }

}