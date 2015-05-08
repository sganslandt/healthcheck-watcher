package org.sganslandt.watcher;

import com.google.common.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RecordingEventBus extends EventBus {

    private final List<Object> recordedEvents = new LinkedList<>();
    private final EventBus delegate;

    public RecordingEventBus(EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public void post(Object event) {
        this.recordedEvents.add(event);
        this.delegate.post(event);
    }

    @Override
    public void register(Object  object) {
        this.delegate.register(object);
    }

    @Override
    public void unregister(Object object) {
        this.delegate.unregister(object);
    }

    public void clearRecordedEvents() {
        this.recordedEvents.clear();
    }

    public void expectPublishedEvents(Object... expectedEvents) {
        assertEquals("Wrong events seem to have been published. " + recordedEvents,
                expectedEvents.length, recordedEvents.size());

        for (int i = 0; i != expectedEvents.length ; i++)
            assertEquals("Unexpected event at position " + i, expectedEvents[i], recordedEvents.get(i));
    }

    public void expectNoPublishedEvents() {
        assertEquals(0, recordedEvents.size());
    }
}
