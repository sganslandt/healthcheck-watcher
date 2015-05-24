package org.sganslandt.watcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ViewSettings {
    public static final int MAX_COLUMNS = 12;
    private final int refreshInterval;
    private final int columns;

    @JsonCreator
    public ViewSettings(@JsonProperty("refreshInterval") final int refreshRate,
                        @JsonProperty("columns") final int columns) {
        this.refreshInterval = refreshRate;
        this.columns = columns;
    }

    public int getColumnWidth() {
        return MAX_COLUMNS / columns;
    }
}
