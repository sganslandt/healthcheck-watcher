package org.sganslandt.watcher;

import com.google.common.eventbus.EventBus;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.sganslandt.watcher.api.events.NodeAddedEvent;
import org.sganslandt.watcher.api.events.ServiceAddedEvent;
import org.sganslandt.watcher.api.viewmodels.SystemViewModel;
import org.sganslandt.watcher.core.ServiceDAO;
import org.sganslandt.watcher.core.TableUpdater;
import org.sganslandt.watcher.external.JerseyHealthCheckerClient;
import org.sganslandt.watcher.resources.SystemResource;
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
        bootstrap.addBundle(new ViewBundle<Configuration>());
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
        final EventBus eventBus = new EventBus();

        // The HealthCheckerClient
        final Client client =
                new JerseyClientBuilder(environment)
                        .using(configuration.getHttpClient())
                        .build(getName());
        final JerseyHealthCheckerClient healthCheckerClient = new JerseyHealthCheckerClient(client);

        // DB and TableUpdater
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "servicesDataSource");
        final ServiceDAO dao = jdbi.onDemand(ServiceDAO.class);
        dao.createServicesTable();
        dao.createURLsTable();
        final TableUpdater tableUpdater = new TableUpdater(dao);

        // Setup the System
        final org.sganslandt.watcher.core.System system = configuration.getSystem().build(healthCheckerClient, eventBus);
        SystemViewModel systemViewModel = new SystemViewModel(configuration.getSystem().getSystemName());

        // The Jersey Resource
        final SystemResource healthResource = new SystemResource(system, systemViewModel, configuration.getViewSettings());

        // Subscribe (almost) everything to the EventBus
        eventBus.register(system);
        eventBus.register(healthResource);
        eventBus.register(systemViewModel);

        // "Replay" state to everyone
        replay(dao, eventBus);

        // Subscribe listeners that can't take replays...
        eventBus.register(tableUpdater);

        // Exprose the Jersey Resource
        environment.jersey().register(healthResource);
    }

    public void replay(final ServiceDAO dao, final EventBus eventBus) {
        for (String serviceName : dao.listServices()) {
            eventBus.post(new ServiceAddedEvent(serviceName));
            for (String url : dao.listNodes(serviceName))
                eventBus.post(new NodeAddedEvent(serviceName, url));
        }
    }

}