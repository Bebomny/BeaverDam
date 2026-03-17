package dev.bebomny.beaverdam.downloader.listeners;

import dev.bebomny.beaverdam.common.events.AnimeItemDownloadRequestEvent;
import dev.bebomny.beaverdam.downloader.services.TorrentManagerService;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.ApplicationModuleListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DownloadEventListener {

    private final WatchpostQueryApi watchpostQueryApi;
    private final TorrentManagerService torrentManagerService;

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
        }, () -> log.atError().log("Could not find download details for item Id: {}", event.animeItemId()));
    }
}
