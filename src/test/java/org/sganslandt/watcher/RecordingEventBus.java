package org.sganslandt.watcher;

import com.google.common.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

class RecordingEventBus extends EventBus {

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

    public void clearRecordedEvents() {
        this.recordedEvents.clear();
    }

    public List<Object> getRecordedEvents() {
        return this.recordedEvents;
    }

    public void expectPublishedEvents(Object... events) {
        assertEquals(events.length, recordedEvents.size());
        for (int i = 0; i != events.length; i++)
            assertEquals("Unexpected event at position " + i, events[i], recordedEvents.get(i));
    }

    public void expectNoPublishedEvents() {
        assertEquals(0, recordedEvents.size());
    }
}
