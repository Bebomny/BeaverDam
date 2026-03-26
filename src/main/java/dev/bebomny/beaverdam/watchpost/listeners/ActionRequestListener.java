package dev.bebomny.beaverdam.watchpost.listeners;

import dev.bebomny.beaverdam.common.events.WatchpostRefetchFeedsRequestEvent;
import dev.bebomny.beaverdam.watchpost.WatchpostScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionRequestListener {

    private final WatchpostScheduler watchpostScheduler;

    @Async
    @EventListener
    public void onManualFetchRequest(WatchpostRefetchFeedsRequestEvent event) {
        log.atInfo().log("Manual Rss feed refetch triggered from {} by {}", event.source(), event.requester());

        try {
            watchpostScheduler.executeFetchCycle();
            log.atInfo().log("Manual Rss fetch completed successfully");
        } catch (Exception e) {
            log.atError().log("Manual Rss feed refetch failed!", e);
        }
    }
}
