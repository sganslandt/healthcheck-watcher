package org.sganslandt.watcher.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Health {
    final boolean healthy;
    final Optional<String> message;

    @JsonCreator
    public Health(@JsonProperty("healthy") boolean healthy, @JsonProperty("message") String message) {
        this.healthy = healthy;
        this.message = message != null ? Optional.of(message) : Optional.<String>absent();
    }
}
