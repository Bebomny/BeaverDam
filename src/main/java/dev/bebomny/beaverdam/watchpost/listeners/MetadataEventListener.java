package dev.bebomny.beaverdam.watchpost.listeners;

import dev.bebomny.beaverdam.common.events.WatchpostNewShowSeriesEvent;
import dev.bebomny.beaverdam.watchpost.dto.AniListDto;
import dev.bebomny.beaverdam.watchpost.entities.ShowMetadata;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.repos.ShowMetadataRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import dev.bebomny.beaverdam.watchpost.services.AniListApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataEventListener {

    private final AniListApiService aniListService;
    private final ShowMetadataRepository metadataRepository;
    private final ShowSeriesRepository showSeriesRepository;

    @Async
    @EventListener
    public void onNewShowSeries(WatchpostNewShowSeriesEvent event) {
        ShowSeries series = showSeriesRepository.findById(event.showSeriesId()).orElseThrow();
        String title = series.getSeriesName();

        log.atInfo().log("Fetching AniList metadata for new series[{}]: {}", event.showSeriesId(), title);

        Optional<AniListDto.Media> mediaOpt = aniListService.fetchAnimeMetadata(title, event.showSeriesId());
        mediaOpt.ifPresent(media -> {
            ShowMetadata metadata = ShowMetadata.builder()
                    .showSeries(series)
                    .anilistId(media.id())
                    .malId(media.idMal())
                    .localizedName(media.getBestTitle())
                    .coverImageUrl(media.coverImage() != null ? media.coverImage().large() : null)
                    .synopsis(media.description())
                    .genres(media.getGenresAsString())
                    .status(media.status())
                    .build();

            metadataRepository.save(metadata);
            log.atInfo().log("Successfully saved metadata for: {}", title);
        });
    }
}
