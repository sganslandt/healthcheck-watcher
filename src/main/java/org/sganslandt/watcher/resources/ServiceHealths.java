package org.sganslandt.watcher.resources;

import lombok.Data;

import java.util.List;

@Data
final class ServiceHealths {
    private final String serviceName;
    private final String state = "N/A";
    private final List<InstanceHealths> instances;
}
