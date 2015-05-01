package org.sganslandt.watcher.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Optional;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Health {
    final String name;
    final boolean healthy;
    final Optional<String> message;

    public Health(String name, boolean healthy, Optional<String> message) {
        this.name = name;
        this.healthy = healthy;
        this.message = message;
    }

    public String getMessage() {
        return message.orNull();
    }
}
