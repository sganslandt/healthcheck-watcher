package org.sganslandt.watcher;

import com.google.common.eventbus.EventBus;

import static org.junit.Assert.assertEquals;

public class TestFixture {

    private final RecordingEventBus eventBus;

    public TestFixture() {
        eventBus = new RecordingEventBus(new EventBus());
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }

    public TestFixture given(Object... events) {
        eventBus.clearRecordedEvents();
        for (Object e : events)
            eventBus.post(e);
        return this;
    }

    public TestFixture when(Object event) {
        eventBus.clearRecordedEvents();
        eventBus.post(event);
        return this;
    }

    public TestFixture when(Runnable r) {
        eventBus.clearRecordedEvents();
        r.run();
        return this;
    }

    public void expectedPublishEvents(Object... expectedEvents) {
        eventBus.expectPublishedEvents(expectedEvents);
    }

}
