package org.sganslandt.watcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ViewSettings {
    private final int refreshRate;
    private final String systemName;

    @JsonCreator
    public ViewSettings(@JsonProperty("refreshRate") final int refreshRate,
                        @JsonProperty("systemName") final String systemName) {
        this.refreshRate = refreshRate;
        this.systemName = systemName;
    }
}
