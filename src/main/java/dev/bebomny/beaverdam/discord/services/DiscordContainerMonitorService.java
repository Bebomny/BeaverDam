package dev.bebomny.beaverdam.discord.services;

import dev.bebomny.beaverdam.discord.entities.MonitoredContainer;
import dev.bebomny.beaverdam.discord.repos.MonitoredContainerRepository;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordContainerMonitorService {

    private final MonitoredContainerRepository monitoredContainerRepository;
    private final DockerCommandApi dockerCommandApi;

    private final Map<String, String> consoleChannels = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadConsoleChannels() {
        monitoredContainerRepository.findAll().forEach(mapping ->
                consoleChannels.put(mapping.getDiscordChannelId(), mapping.getContainerName()));
    }

    public Optional<String> findContainerNameByChannelId(String channelId) {
        return Optional.ofNullable(consoleChannels.get(channelId));
    }

    @Transactional
    public void registerMonitoredContainer(
            String containerName,
            String channelId,
            String formatterType,
            String strategy,
            String runAs,
            boolean autoAttach) {
        MonitoredContainer mapping = MonitoredContainer.builder()
                .containerName(containerName)
                .discordChannelId(channelId)
                .formatterType(formatterType)
                .build();

        monitoredContainerRepository.saveAndFlush(mapping);
        log.atInfo().log("Saved Discord MonitoredContainer mapping for container: {}", containerName);

        dockerCommandApi.registerNewContainer(containerName, strategy, runAs, autoAttach);

        Runnable cacheUpdate = () -> updateConsoleChannelCache(channelId, containerName);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cacheUpdate.run();
                }
            });
        } else {
            cacheUpdate.run();
        }
    }

    private void updateConsoleChannelCache(String channelId, String containerName) {
        consoleChannels.values().removeIf(existing -> existing.equals(containerName));
        consoleChannels.put(channelId, containerName);
    }
}
