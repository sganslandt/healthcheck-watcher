package org.sganslandt.watcher.core;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sganslandt.watcher.Configuration;
import org.sganslandt.watcher.TestFixture;
import org.sganslandt.watcher.api.events.*;
import org.sganslandt.watcher.external.Health;
import org.sganslandt.watcher.external.HealthCheckerClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class HealthAggregationTest {

    private final HealthCheckerClient healthCheckerClient = mock(HealthCheckerClient.class);
    private String systemName;
    private TestFixture fixture;
    private EventBus eventBus;
    private System system;

    @Before
    public void setup() throws Exception {
        fixture = new TestFixture();
        eventBus = fixture.getEventBus();

        systemName = "testSystem";
        system = new System(systemName, healthCheckerClient, 10, eventBus);
        eventBus.register(system);
    }

    @After
    public void resetIt() throws InterruptedException {
        reset(healthCheckerClient);
        eventBus.unregister(system);
    }

    @Test
    public void testSingleNode_transitionsToHealthy() {
        final String serviceName = "service";
        final String url = "url";

        final Map<String, Health> healths = new HashMap<>();
        healths.put("foo", new Health(true, null));
        when(healthCheckerClient.check(url)).thenReturn(healths);

        fixture.given(
                new ServiceAddedEvent(serviceName),
                new SystemStateChangedEvent(systemName, System.State.Healthy),
                new NodeAddedEvent(serviceName, url),
                new ServiceStateChangedEvent(serviceName, Service.State.Unhealthy),
                new SystemStateChangedEvent(systemName, System.State.Unhealthy)
        ).when(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).expectedPublishEvents(
                new NodeHealthChangedEvent(serviceName, url, Node.State.Healthy, Arrays.asList(new org.sganslandt.watcher.core.Health("foo", true, Optional.<String>absent()))),
                new ServiceStateChangedEvent(serviceName, Service.State.Healthy),
                new SystemStateChangedEvent(systemName, System.State.Healthy)
        );
    }

    @Test
    public void testSingleNode_transitionsToUnhealthy() {
        final String serviceName = "service2";
        final String url = "url2";

        final Map<String, Health> healths = new HashMap<>();
        healths.put("foo", new Health(false, "bar"));
        when(healthCheckerClient.check(url)).thenReturn(healths);

        fixture.given(
                new ServiceAddedEvent(serviceName),
                new SystemStateChangedEvent(systemName, System.State.Healthy),
                new NodeAddedEvent(serviceName, url),
                new ServiceStateChangedEvent(serviceName, Service.State.Unhealthy),
                new SystemStateChangedEvent(systemName, System.State.Unhealthy),
                new NodeHealthChangedEvent(serviceName, url, Node.State.Healthy, Arrays.asList(new org.sganslandt.watcher.core.Health("foo", true, Optional.<String>absent()))),
                new ServiceStateChangedEvent(serviceName, Service.State.Healthy),
                new SystemStateChangedEvent(systemName, System.State.Healthy)
        ).when(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).expectedPublishEvents(
                new NodeHealthChangedEvent(serviceName, url, Node.State.Unhealthy, Arrays.asList(new org.sganslandt.watcher.core.Health("foo", false, Optional.of("bar")))),
                new ServiceStateChangedEvent(serviceName, Service.State.Unhealthy),
                new SystemStateChangedEvent(systemName, System.State.Unhealthy)
        );
    }

}
