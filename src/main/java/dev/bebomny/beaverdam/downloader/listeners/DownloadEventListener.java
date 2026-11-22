package dev.bebomny.beaverdam.downloader.listeners;

import dev.bebomny.beaverdam.downloader.events.TorrentDownloadFinishedEvent;
import dev.bebomny.beaverdam.downloader.events.TorrentDownloadRequestEvent;
import dev.bebomny.beaverdam.downloader.services.TorrentManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadEventListener {

    private final TorrentManagerService torrentManagerService;
    private final ApplicationEventPublisher eventPublisher;

    @ApplicationModuleListener
    public void onDownloadRequested(TorrentDownloadRequestEvent event) {
        log.atInfo().log("Received TorrentDownloadRequestEvent for reference Id: {}", event.referenceId());

        torrentManagerService.processDownload(
                event.torrentUrl(),
                event.torrentName(),
                event.category(),
                event.tags(),
                event.customShareRatio()
        );

        eventPublisher.publishEvent(new TorrentDownloadFinishedEvent(event.referenceId()));
    }
}
