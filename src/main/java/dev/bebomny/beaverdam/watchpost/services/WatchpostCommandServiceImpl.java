package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.downloader.events.TorrentDownloadRequestEvent;
import dev.bebomny.beaverdam.watchpost.WatchpostCommandApi;
import dev.bebomny.beaverdam.watchpost.api.RssFeedSaveResult;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesParam;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesUpdateRequest;
import dev.bebomny.beaverdam.watchpost.dto.AniListDto;
import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import dev.bebomny.beaverdam.watchpost.entities.RssFeed;
import dev.bebomny.beaverdam.watchpost.entities.ShowMetadata;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.RssFeedRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowMetadataRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchpostCommandServiceImpl implements WatchpostCommandApi {

    private final RssFeedRepository rssFeedRepository;
    private final ShowSeriesRepository showSeriesRepository;
    private final AnimeRssItemRepository animeRssItemRepository;
    private final AniListApiService aniListApiService;
    private final ShowMetadataRepository showMetadataRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${watchpost.downloader.default_tag:beaverdam}")
    private String defaultDownloadTag = "beaverdam";

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
    public ShowSeriesStateDetailsResult updateShowSeriesParam(Long targetItemId, ShowSeriesParam param, Boolean newValue) {
        ShowSeries series = showSeriesRepository.findById(targetItemId)
                .orElseThrow(() -> new IllegalArgumentException("Series id: " + targetItemId + "not found"));

        switch (param) {
            case INTERESTING -> series.markInteresting(newValue);
            case IGNORED -> series.markIgnored(newValue);
            case AUTODOWNLOAD -> series.setAutoDownloadEnabled(newValue);
            case SUBMITTED -> series.markSubmitted();
        }

//        showSeriesRepository.save(series);
        log.atInfo().log("Updated {} param {} to {}!", series.getSeriesName(), param.toString(), newValue.toString());

        return ShowSeriesStateDetailsResult.builder()
                .id(targetItemId)
                .name(series.getSeriesName())
                .interesting(series.getIsInteresting())
                .ignored(series.getIsIgnored())
                .autoDownload(series.getAutoDownload())
                .submitted(series.getSubmitted())
                .customShareRatio(series.getCustomShareRatio())
                .lastSeen(series.getLastSeen())
                .addedOn(series.getAddedOn())
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

    @Override
    @Transactional
    public boolean removeMetadata(Long showSeriesId) {
        if (showMetadataRepository.findByShowSeriesId(showSeriesId).isEmpty()) {
            log.atWarn().log("Tried to remove metadata for series Id {}, but the metadata is missing!", showSeriesId);
                return false;
        }
        showMetadataRepository.removeByShowSeriesId(showSeriesId);
        log.atInfo().log("Removed metadata for show ith Id {}", showSeriesId);
        return true;
    }

    @Override
    @Transactional
    public void updateShowSeriesState(Long showSeriesId, ShowSeriesUpdateRequest request) {
        ShowSeries series = showSeriesRepository.findById(showSeriesId)
                .orElseThrow(() -> new NoSuchElementException("Series id: " + showSeriesId + " not found"));

        series.applyStateUpdate(
                request.interesting(),
                request.ignored(),
                request.autoDownload(),
                request.customShareRatio()
        );

        showSeriesRepository.save(series);
    }

    @Override
    @Transactional
    public boolean requestAnimeItemDownload(Long animeItemId) {
        Optional<AnimeRssItem> itemOpt = animeRssItemRepository.findWithSeriesById(animeItemId);
        if (itemOpt.isEmpty()) {
            log.atWarn().log("Requested download for non-existent anime item Id: {}", animeItemId);
            return false;
        }

        AnimeRssItem item = itemOpt.get();
        String safeTorrentName = String.format("%s - %s", item.getSeriesName(), item.getEpisode());
        String customShareRatio = item.getShowSeries() != null ? item.getShowSeries().getCustomShareRatio() : null;

        eventPublisher.publishEvent(new TorrentDownloadRequestEvent(
                item.getId(),
                item.getFileLink(),
                safeTorrentName,
                null,
                defaultDownloadTag,
                customShareRatio
        ));
        log.atInfo().log("Published download request for anime item Id: {} with tag '{}'", animeItemId, defaultDownloadTag);
        return true;
    }
}
