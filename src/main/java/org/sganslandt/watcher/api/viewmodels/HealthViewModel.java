package org.sganslandt.watcher.api.viewmodels;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Optional;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthViewModel {
    private final String name;
    private final boolean healthy;
    private final Optional<String> message;

    public String getMessage() {
        return message.orNull();
    }
}
