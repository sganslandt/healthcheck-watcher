package org.sganslandt.watcher;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sganslandt.watcher.core.HealthChecker;
import org.sganslandt.watcher.core.ServiceDOA;
import org.sganslandt.watcher.core.events.NodeAddedEvent;
import org.sganslandt.watcher.core.events.NodeRemovedEvent;
import org.sganslandt.watcher.core.events.ServiceAddedEvent;
import org.sganslandt.watcher.core.events.ServiceRemovedEvent;
import org.sganslandt.watcher.external.HealthCheckerClient;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class NodeManagementTest {
    private final Environment environment = new Environment("", new ObjectMapper(), null, new MetricRegistry(), Object.class.getClassLoader());
    private final Configuration config = new Configuration();
    private final HealthCheckerClient healthCheckerClient = mock(HealthCheckerClient.class);
    private HealthChecker healthChecker;
    private RecordingEventBus recordingEventBus;

    @Before
    public void setup() throws Exception {
        final DataSourceFactory database = new DataSourceFactory();
        database.setDriverClass("org.h2.Driver");
        database.setUser("sa");
        database.setPassword("sa");
        database.setUrl("jdbc:h2:mem:");
        database.setValidationQuery("select 1");
        config.setDataSourceFactory(database);

        config.setHttpClient(new JerseyClientConfiguration());
        config.setEventBus(new EventBusFactory(false, 0));

        final ServiceDOA dao = new DBIFactory().build(environment, database, "servicesDataSource").onDemand(ServiceDOA.class);
        dao.createServicesTable();
        dao.createURLsTable();

        final EventBus eventBus = config.getEventBus().build();
        recordingEventBus = new RecordingEventBus(eventBus);
        eventBus.register(recordingEventBus);

        healthChecker = new HealthChecker(healthCheckerClient, dao, recordingEventBus);

    }

    @After
    public void resetIt() {
        reset(healthCheckerClient);
        recordingEventBus.clearRecordedEvents();
    }

    @Test
    public void testAddService_serviceAdded() {
        // Add a service, published event
        final String serviceName = "foo-service";
        healthChecker.addService(serviceName);

        recordingEventBus.expectPublishedEvents(new ServiceAddedEvent(serviceName));

        // Add it again, should not produce another event
        recordingEventBus.clearRecordedEvents();
        healthChecker.addService(serviceName);

        recordingEventBus.expectNoPublishedEvents();
    }

    @Test
    public void testAddNode_nodeGetsAddedAndIsMonitored() {
        // Add a node without the service being there, service and node are added
        final String serviceName = "foo-service";
        final String url = "foo-url";
        healthChecker.monitor(serviceName, url);

        recordingEventBus.expectPublishedEvents(
                new ServiceAddedEvent(serviceName),
                new NodeAddedEvent(serviceName, url)
        );

        // Add another node, only node is added
        recordingEventBus.clearRecordedEvents();
        final String url2 = "foo-url2";
        healthChecker.monitor(serviceName, url2);

        recordingEventBus.expectPublishedEvents(
                new NodeAddedEvent(serviceName, url2)
        );

        // Add the same node again, nothing happens
        recordingEventBus.clearRecordedEvents();
        healthChecker.monitor(serviceName, url2);

        recordingEventBus.expectNoPublishedEvents();
    }

    @Test
    public void testRemoveNode_nodeIsRemoved() {
        final String serviceName = "foo-service";
        final String url = "foo-url";
        final String url2 = "foo-url2";
        healthChecker.monitor(serviceName, url);
        healthChecker.monitor(serviceName, url2);

        recordingEventBus.clearRecordedEvents();
        healthChecker.stopMonitoring(serviceName, url);
        recordingEventBus.expectPublishedEvents(new NodeRemovedEvent(serviceName, url));

        // Remove it again, nothing happens
        recordingEventBus.clearRecordedEvents();
        healthChecker.stopMonitoring(serviceName, url);
        recordingEventBus.expectPublishedEvents();
    }

    @Test
    public void testRemoveService_allNodesAreRemoved() {
        final String serviceName = "foo-service";
        final String url = "foo-url";
        final String url2 = "foo-url2";
        healthChecker.monitor(serviceName, url);
        healthChecker.monitor(serviceName, url2);

        // Remove the service, service and all nodes are removed
        recordingEventBus.clearRecordedEvents();
        healthChecker.removeService(serviceName);
        recordingEventBus.expectPublishedEvents(
                new NodeRemovedEvent(serviceName, url),
                new NodeRemovedEvent(serviceName, url2),
                new ServiceRemovedEvent(serviceName)
        );

        // Remove it again, nothing happens
        recordingEventBus.clearRecordedEvents();
        healthChecker.removeService(serviceName);
        recordingEventBus.expectNoPublishedEvents();
    }


}
