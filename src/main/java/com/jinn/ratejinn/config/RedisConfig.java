package com.jinn.ratejinn.config;

import com.jinn.ratejinn.config.validation.ValidRedisConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.stream.Collectors;

@Data
@ValidRedisConfig
public class RedisConfig {
    @NotBlank
    @Pattern(regexp = "standalone|cluster", message = "Mode must be 'standalone' or 'cluster'")
    private String mode;

    @Valid
    private NodeConfig standalone;

    @Valid
    @NestedConfigurationProperty
    private ClusterConfig cluster;

    @Data
    public static class NodeConfig {
        @NotBlank
        private String host;

        @Min(1)
        @Max(65535)
        private int port;

        public NodeConfig() {}

        public NodeConfig(String hostPort) {
            String[] parts = hostPort.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid node format. Expected host:port");
            }
            this.host = parts[0];
            this.port = Integer.parseInt(parts[1]);
        }
    }

    @Data
    public static class ClusterConfig {
        @NotEmpty
        @Size(min = 1, max = 3)
        private List<NodeConfig> nodes;

        public void setNodes(List<String> nodeStrings) {
            if (nodeStrings != null) {
                this.nodes = nodeStrings.stream()
                        .map(NodeConfig::new)
                        .collect(Collectors.toList());
            }
        }
    }
}
