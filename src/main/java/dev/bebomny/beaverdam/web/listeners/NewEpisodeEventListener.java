package dev.bebomny.beaverdam.web.listeners;

import dev.bebomny.beaverdam.common.events.WatchpostNewAnimeItemEvent;
import dev.bebomny.beaverdam.web.services.SseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewEpisodeEventListener {

    private final SseNotificationService sseNotificationService;

    @ApplicationModuleListener
    public void onNewEpisode(WatchpostNewAnimeItemEvent event) {
        sseNotificationService.broadcastNewEpisodeEvent(event.isInteresting());
    }
}
