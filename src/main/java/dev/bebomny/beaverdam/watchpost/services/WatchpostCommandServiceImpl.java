package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.common.dtos.RssFeedSaveResult;
import dev.bebomny.beaverdam.common.dtos.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.common.events.types.ShowSeriesParam;
import dev.bebomny.beaverdam.watchpost.WatchpostCommandApi;
import dev.bebomny.beaverdam.watchpost.dto.AniListDto;
import dev.bebomny.beaverdam.watchpost.entities.RssFeed;
import dev.bebomny.beaverdam.watchpost.entities.ShowMetadata;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.repos.RssFeedRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowMetadataRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchpostCommandServiceImpl implements WatchpostCommandApi {

    private final RssFeedRepository rssFeedRepository;
    private final ShowSeriesRepository showSeriesRepository;
    private final AniListApiService aniListApiService;
    private final ShowMetadataRepository showMetadataRepository;

    @Override
    @Transactional
    public RssFeedSaveResult addNewRssFeed(String feedName, String feedProcessor, String feedUrl, Long pollInterval, Boolean enabled) {
        if (rssFeedRepository.existsByFeedUrl(feedUrl)) {
            log.atInfo().log("RssFeed with an identical url already exists. Consider changing the poll interval instead");
            return RssFeedSaveResult.builder()
                    .success(false)
                    .feedName(feedName)
                    .feedProcessor(feedProcessor)
                    .feedUrl(feedUrl)
                    .pollInterval(pollInterval)
                    .enabled(enabled)
                    .build();
        }

        RssFeed newFeed = RssFeed.builder()
                .feedName(feedName)
                .rssReaderName(feedProcessor)
                .feedUrl(feedUrl)
                .pollInterval(pollInterval)
                .enabled(enabled)
                .build();

        rssFeedRepository.save(newFeed);

        return RssFeedSaveResult.builder()
                .success(true)
                .feedName(feedName)
                .feedProcessor(feedProcessor)
                .feedUrl(feedUrl)
                .pollInterval(pollInterval)
                .enabled(enabled)
                .build();
    }

    @Override
    @Transactional
    public ShowSeriesStateDetailsResult updateSeriesState(Long targetItemId, ShowSeriesParam param, Boolean newValue) {
        ShowSeries series = showSeriesRepository.findById(targetItemId)
                .orElseThrow(() -> new IllegalArgumentException("Series id: " + targetItemId + "not found"));

        switch (param) {
            case INTERESTING -> {
                if (newValue) {
                    series.setIsInteresting(true);
                    series.setIsIgnored(false);
                } else {
                    series.setIsInteresting(false);
                    series.setAutoDownload(false);
                }
            }

            case IGNORED ->  {
                if (newValue) {
                    series.setIsIgnored(true);
                    series.setIsInteresting(false);
                    series.setAutoDownload(false);
                } else {
                    series.setIsIgnored(false);
                }
            }

            case AUTODOWNLOAD ->  {
                if (newValue) {
                    series.setAutoDownload(true);
                    series.setIsInteresting(true);
                    series.setIsIgnored(false);
                } else {
                    series.setAutoDownload(false);
                }
            }

            case SUBMITTED -> {
                series.setSubmitted(true);
            }
        }

//        showSeriesRepository.save(series);
        log.atInfo().log("Updated {} param {} to {}!", series.getSeriesName(), param.toString(), newValue.toString());

        return ShowSeriesStateDetailsResult.builder()
                .id(targetItemId)
                .name(series.getSeriesName())
                .interesting(series.getIsInteresting())
                .ignored(series.getIsIgnored())
                .autoDownload(series.getAutoDownload())
                .build();
    }

    @Override
    @Transactional
    public void manuallyAssignAniListIdAndFetchMetadata(Long showSeriesId, Long aniListId) {
        ShowSeries series = showSeriesRepository.findById(showSeriesId)
                .orElseThrow(() -> new IllegalArgumentException("Series id: " + showSeriesId + " not found"));

        AniListDto.Media media = aniListApiService.fetchAnimeMetadataById(showSeriesId, aniListId)
                .orElseThrow(() -> new IllegalArgumentException("AniListId: " + aniListId + " not found"));

        ShowMetadata metadata = showMetadataRepository.findByShowSeriesId(showSeriesId)
                .orElse(ShowMetadata.builder().showSeries(series).build());

        metadata.setAnilistId(media.id());
        metadata.setMalId(media.idMal());
        metadata.setLocalizedName(media.getBestTitle());
        metadata.setCoverImageUrl(media.coverImage() != null ? media.coverImage().large() : null);
        metadata.setSynopsis(media.description());
        metadata.setGenres(media.getGenresAsString());
        metadata.setStatus(media.status());

        showMetadataRepository.save(metadata);
        log.atInfo().log("Manual anilistId assignment for id {} '{}'. Successful",  aniListId, media.getBestTitle());
    }
}
