package dev.bebomny.beaverdam.dockerintegration.services;

import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import dev.bebomny.beaverdam.dockerintegration.entities.DockerContainerConfig;
import dev.bebomny.beaverdam.dockerintegration.repos.DockerContainerConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandApiServiceImpl implements DockerCommandApi {

    private final DockerCommandService commandService;
    private final DockerLogMonitorService logMonitorService;
    private final DockerContainerConfigRepository configRepository;

    @Override
    public void sendCommandToContainer(String containerName, String command) {
        DockerContainerConfig config = configRepository.findById(containerName)
                .orElseThrow(() -> new IllegalArgumentException("No container with name " + containerName));

        commandService.executeCommand(
                containerName,
                config.getCommandStrategyId(),
                command,
                config.getRunAsUser()
        );
    }

    @Override
    @Transactional
    public void registerNewContainer(String containerName, String commandStrategyId, String runAsUser, boolean autoAttach) {
        DockerContainerConfig config = DockerContainerConfig.builder()
                .containerName(containerName)
                .commandStrategyId(commandStrategyId)
                .runAsUser(runAsUser)
                .autoAttachOnStart(autoAttach)
                .build();

        configRepository.save(config);
        log.atInfo().log("Saved new Docker container config for '{}'", containerName);

        if (autoAttach) {
            logMonitorService.startMonitoring(containerName);
        }
    }

    @Override
    public void unregisterContainer(String containerName) {

    }
}
