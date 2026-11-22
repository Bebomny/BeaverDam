package dev.bebomny.beaverdam.watchpost.listeners;

import dev.bebomny.beaverdam.downloader.events.TorrentDownloadFinishedEvent;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StatusTrackerListener {

    private final AnimeRssItemRepository animeItemRepository;

    @ApplicationModuleListener
    public void onDownloadFinished(TorrentDownloadFinishedEvent event) {
        animeItemRepository.findById(event.referenceId()).ifPresent(animeItem -> {
            animeItem.setDownloaded(true);
            animeItem.setDownloadedOn(LocalDateTime.now());

            animeItemRepository.save(animeItem);
        });
    }
}
