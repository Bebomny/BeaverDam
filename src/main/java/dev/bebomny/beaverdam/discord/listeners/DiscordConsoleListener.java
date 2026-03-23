package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.discord.repos.MonitoredContainerRepository;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordConsoleListener extends ListenerAdapter {

    private final MonitoredContainerRepository containerRepository;
    private final DockerCommandApi dockerCommandApi;

    private final Map<String, String> consoleChannels = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadConsoleChannels() {
        containerRepository.findAll().forEach(mapping -> {
            consoleChannels.put(mapping.getDiscordChannelId(), mapping.getContainerName());
        });
    }

    public void registerChannel(String channelId, String containerName) {
        consoleChannels.put(channelId, containerName);
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        String channelId = event.getChannel().getId();

        String containerName = consoleChannels.get(channelId);
        if (containerName == null) {
            return;
        }

        String rawCommand = event.getMessage().getContentRaw();

        CompletableFuture.runAsync(() -> {
            try {
                dockerCommandApi.sendCommandToContainer(containerName, rawCommand);
            } catch (Exception e) {
                log.atError().log("Failed to send command from Discord to {}", containerName, e);
            }
        });
    }
}
