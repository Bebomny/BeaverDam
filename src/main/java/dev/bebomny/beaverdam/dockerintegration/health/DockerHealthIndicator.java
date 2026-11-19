package dev.bebomny.beaverdam.dockerintegration.health;

import com.github.dockerjava.api.DockerClient;
import dev.bebomny.beaverdam.dockerintegration.repos.DockerContainerConfigRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DockerHealthIndicator implements HealthIndicator {

    private final DockerClient dockerClient;
    private final DockerContainerConfigRepository containerConfigRepository;

    @Override
    public @Nullable Health health() {
        try {
            dockerClient.pingCmd().exec();

            long configuredContainersCount = containerConfigRepository.count();

            return Health.up()
                    .withDetail("docker_daemon", "reachable")
                    .withDetail("configured_containers_count", configuredContainersCount)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("docker_daemon", "unreachable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
