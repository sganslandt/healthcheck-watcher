package org.sganslandt.watcher.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import lombok.Data;

@Data
public class HealthResult {
    final boolean healthy;
    final Optional<String> message;

    @JsonCreator
    public HealthResult(@JsonProperty("healthy") boolean healthy, @JsonProperty("message") String message) {
        this.healthy = healthy;
        this.message = message != null ? Optional.of(message) : Optional.<String>absent();
    }
}
