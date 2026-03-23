package dev.bebomny.beaverdam.dockerintegration.repos;

import dev.bebomny.beaverdam.dockerintegration.entities.DockerContainerConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DockerContainerConfigRepository extends JpaRepository<DockerContainerConfig, String> {
    List<DockerContainerConfig> findByAutoAttachOnStartTrue();
}
