package dev.bebomny.beaverdam.dockerintegration.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import dev.bebomny.beaverdam.common.dtos.DockerPublishedServer;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import dev.bebomny.beaverdam.dockerintegration.entities.DockerContainerConfig;
import dev.bebomny.beaverdam.dockerintegration.repos.DockerContainerConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandApiServiceImpl implements DockerCommandApi {

    private final DockerCommandService commandService;
    private final DockerLogMonitorService logMonitorService;
    private final DockerContainerConfigRepository configRepository;
    private final DockerClient dockerClient;

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

    @Override
    public List<DockerPublishedServer> getPublishedServers(boolean showAll) {
        final String ADDRESS_LABEL = "beaverdam.docker.public-address";
        final String VERSION_LABEL = "beaverdam.docker.public-version";
        final String ADDITIONAL_MESSAGE = "beaverdam.docker.message";
        final String EXCLUDE_LABEL = "beaverdam.docker.public-exclude"; //This allows for some server to be hidden from the public only visible to me for status updates

        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();

        return containers.stream()
                .filter(c -> showAll || !c.getLabels().containsKey(EXCLUDE_LABEL))
                .filter(c -> c.getLabels() != null && c.getLabels().containsKey(ADDRESS_LABEL) && c.getLabels().containsKey(VERSION_LABEL))
                .map(c -> {
                    String name = c.getNames()[0].substring(1);
                    String address = c.getLabels().get(ADDRESS_LABEL);
                    String versionLabel = c.getLabels().get(VERSION_LABEL);
                    boolean isOnline = "running".equalsIgnoreCase(c.getState());

                    String message = null;
                    if (c.getLabels().containsKey(ADDITIONAL_MESSAGE)) {
                        message = c.getLabels().get(ADDITIONAL_MESSAGE);
                    }


                    String statusText = c.getStatus();
                    return new DockerPublishedServer(name, address, isOnline, statusText, versionLabel, message);
                })
                .toList();
    }
}
