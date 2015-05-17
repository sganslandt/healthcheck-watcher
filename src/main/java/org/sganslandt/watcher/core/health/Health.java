package org.sganslandt.watcher.core.health;

import com.google.common.base.Optional;
import lombok.Data;

@Data
public final class Health {
    final String name;
    final boolean healthy;
    final Optional<String> message;

    public Health(String name, boolean healthy, Optional<String> message) {
        this.name = name;
        this.healthy = healthy;
        this.message = message;
    }

}
