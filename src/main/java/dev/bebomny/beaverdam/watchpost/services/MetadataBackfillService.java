package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.watchpost.dto.AniListDto;
import dev.bebomny.beaverdam.watchpost.entities.ShowMetadata;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.repos.ShowMetadataRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataBackfillService {

    private final AniListApiService aniListService;
    private final ShowMetadataRepository metadataRepository;
    private final ShowSeriesRepository showSeriesRepository;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void runBackfillOnStartup() {
        List<ShowSeries> missingShowSeries = showSeriesRepository.findSeriesWithoutMetadata();

        if(missingShowSeries.isEmpty()) {
            log.atInfo().log("AniList Backfill: All {} shows are fully populated. Skipping...", showSeriesRepository.count());
            return;
        }

        log.atInfo().log("AniList Backfill: Found {} shows missing metadata. Starting background sync...", missingShowSeries.size());
        long startTime = System.currentTimeMillis();

        int count = 0;
        for (ShowSeries series : missingShowSeries) {
            String title = series.getSeriesName();

            try {
                Optional<AniListDto.Media> mediaOpt = aniListService.fetchAnimeMetadata(title);

                mediaOpt.ifPresent(media -> {
                    ShowMetadata metadata = ShowMetadata.builder()
                            .showSeries(series)
                            .malId(media.idMal())
                            .anilistId(media.id())
                            .localizedName(media.getBestTitle())
                            .coverImageUrl(media.coverImage() != null ? media.coverImage().large() : null)
                            .synopsis(media.description()).genres(media.getGenresAsString()).status(media.status())
                            .build();

                    metadataRepository.save(metadata);
                });

                count++;
                if (count % 50 == 0) {
                    log.atInfo().log("AniList Backfill Progress: {}/{}", count, missingShowSeries.size());
                }

                Thread.sleep(2050);
            } catch (InterruptedException e) {
                log.atError().log("AniList Backfill was interrupted!");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.atError().log("AniList Backfill failed for series: {}", title, e);
            }
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.atInfo().log("AniList Backfill complete! Processed {} shows in {}ms.", count,  elapsedTime);
    }
}
