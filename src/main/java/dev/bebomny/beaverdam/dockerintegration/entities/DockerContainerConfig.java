package dev.bebomny.beaverdam.dockerintegration.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "container_configs", schema = "docker")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DockerContainerConfig {

    @Id
    private String containerName;

    private String commandStrategyId;
    private String runAsUser;
    private Boolean autoAttachOnStart;
}
