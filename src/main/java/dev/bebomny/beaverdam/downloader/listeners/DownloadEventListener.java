package dev.bebomny.beaverdam.downloader.listeners;

import dev.bebomny.beaverdam.downloader.events.TorrentDownloadFinishedEvent;
import dev.bebomny.beaverdam.downloader.events.TorrentDownloadRequestEvent;
import dev.bebomny.beaverdam.downloader.services.TorrentManagerService;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadEventListener {

    private final WatchpostQueryApi watchpostQueryApi;
    private final TorrentManagerService torrentManagerService;
    private final ApplicationEventPublisher eventPublisher;

    @ApplicationModuleListener
    public void onDownloadRequested(AnimeItemDownloadRequestEvent event) {
        log.atInfo().log("Received DownloadRequestEvent for item Id: {}", event.animeItemId());

        watchpostQueryApi.getDownloadDetailsForAnimeItemById(event.animeItemId()).ifPresentOrElse(details -> {
            String safeTorrentName = String.format("%s - %s", details.seriesName(), details.episode());
            String category = event.category();
            String tags = event.tags();

            torrentManagerService.processDownload(
                    details.torrentUrl(),
                    safeTorrentName,
                    category,
                    tags,
                    details.customShareRatio()
            );

            eventPublisher.publishEvent(new AnimeItemDownloadFinishedEvent(event.animeItemId()));
        }, () -> log.atError().log("Could not find download details for item Id: {}", event.animeItemId()));
    }
}
