package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.discord.services.DiscordContainerMonitorService;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordConsoleListener extends ListenerAdapter {

    private final DiscordContainerMonitorService containerMonitorService;
    private final DockerCommandApi dockerCommandApi;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        String channelId = event.getChannel().getId();

        containerMonitorService.findContainerNameByChannelId(channelId).ifPresent(containerName -> {
            String rawCommand = event.getMessage().getContentRaw();

            CompletableFuture.runAsync(() -> {
                try {
                    dockerCommandApi.sendCommandToContainer(containerName, rawCommand);
                } catch (Exception e) {
                    log.atError().log("Failed to send command from Discord to {}", containerName, e);
                }
            });
        });
    }
}
