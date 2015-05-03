package org.sganslandt.watcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ViewSettings {
    private final int refreshInterval;

    @JsonCreator
    public ViewSettings(@JsonProperty("refreshInterval") final int refreshRate) {
        this.refreshInterval = refreshRate;
    }
}
