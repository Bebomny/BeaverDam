package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.common.events.DockerContainerLogEvent;
import dev.bebomny.beaverdam.discord.DiscordMessagingService;
import dev.bebomny.beaverdam.discord.formatters.DiscordLogFormatter;
import dev.bebomny.beaverdam.discord.formatters.FormatterRegistry;
import dev.bebomny.beaverdam.discord.repos.MonitoredContainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DockerLogListener {

    private final MonitoredContainerRepository containerRepository;
    private final DiscordMessagingService messagingService;
    private final FormatterRegistry formatterRegistry;

    @EventListener
    @Async
    public void onContainerLog(DockerContainerLogEvent event) {
        containerRepository.findById(event.containerName()).ifPresent(config -> {
            DiscordLogFormatter formatter = formatterRegistry.getFormatter(config.getFormatterType());

            String formattedMessage = formatter.formatLog(event);

            messagingService.sendTextMessage(config.getDiscordChannelId(), formattedMessage);
        });
    }
}
