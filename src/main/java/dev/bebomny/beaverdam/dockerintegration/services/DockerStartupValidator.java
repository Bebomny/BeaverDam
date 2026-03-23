package dev.bebomny.beaverdam.dockerintegration.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import dev.bebomny.beaverdam.dockerintegration.entities.DockerContainerConfig;
import dev.bebomny.beaverdam.dockerintegration.repos.DockerContainerConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerStartupValidator implements ApplicationRunner {

    private final DockerClient dockerClient;
    private final DockerContainerConfigRepository configRepository;
    private final DockerLogMonitorService logMonitorService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Sanity check by listing all running containers
            List<Container> containers = dockerClient.listContainersCmd().exec();

            log.atInfo().log("Docker Daemon connected! Found {} running containers:", containers.size());
            for (Container container : containers) {
                String name = container.getNames()[0].substring(1);
                log.atInfo().log("{} (Status: {})", name, container.getStatus());
            }

            //Load onStartup containers to monitor lists
            List<DockerContainerConfig> autoAttachConfigs = configRepository.findByAutoAttachOnStartTrue();

            if (!autoAttachConfigs.isEmpty()) {
                log.atInfo().log("Re-attaching to {} monitored containers...", autoAttachConfigs.size());
                for (DockerContainerConfig config : autoAttachConfigs) {
                    logMonitorService.startMonitoring(config.getContainerName());
                }
            }

        } catch (Exception e) {
            log.atError().log("Could not communicate with Docker Daemon on startup. Is is running?", e);
        }
    }
}
