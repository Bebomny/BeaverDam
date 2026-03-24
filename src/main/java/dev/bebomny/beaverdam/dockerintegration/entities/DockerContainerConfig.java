package dev.bebomny.beaverdam.dockerintegration.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "container_configs", schema = "docker")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DockerContainerConfig {

    @Id
    @EqualsAndHashCode.Include
    private String containerName;

    private String commandStrategyId;
    private String runAsUser;
    private Boolean autoAttachOnStart;
}
