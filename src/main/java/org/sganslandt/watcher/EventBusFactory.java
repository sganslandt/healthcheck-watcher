package org.sganslandt.watcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.Data;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
public class EventBusFactory {
    private final boolean async;
    private final int threads;

    @JsonCreator
    public EventBusFactory(
            @JsonProperty("async") final boolean async,
            @JsonProperty("threads") final int threads) {
        this.async = async;
        this.threads = threads;
    }

    public EventBus build() {
        if (async)
            return new AsyncEventBus(new ThreadPoolExecutor(threads, threads, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
        else
            return new EventBus();
    }

}
