package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.discord.DiscordMessagingService;
import dev.bebomny.beaverdam.discord.TargetChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordStatusListener { //TODO: Rename this class and reposition it in the file structure

    private final DiscordMessagingService messagingService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        messagingService.sendTextMessage(TargetChannel.CONTROL_PANEL, ":beaver: Beaver Dev Bot ready to go!");
    }
}
