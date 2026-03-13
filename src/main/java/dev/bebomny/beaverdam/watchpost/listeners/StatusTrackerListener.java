package dev.bebomny.beaverdam.watchpost.listeners;

import dev.bebomny.beaverdam.common.events.AnimeItemDisplayedEvent;
import dev.bebomny.beaverdam.common.events.AnimeItemDownloadFinishedEvent;
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
    public void onDisplayed(AnimeItemDisplayedEvent event) {
        animeItemRepository.findById(event.animeItemId()).ifPresent(animeItem -> {
            animeItem.setDisplayed(true);

            animeItemRepository.save(animeItem);
        });
    }

    @ApplicationModuleListener
    public void onDownloadFinished(AnimeItemDownloadFinishedEvent event) {
        animeItemRepository.findById(event.animeItemId()).ifPresent(animeItem -> {
            animeItem.setDownloaded(true);
            animeItem.setDownloadedOn(LocalDateTime.now());

            animeItemRepository.save(animeItem);
        });
    }
}
