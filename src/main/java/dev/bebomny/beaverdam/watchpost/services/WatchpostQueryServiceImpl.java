package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.common.dtos.*;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import dev.bebomny.beaverdam.watchpost.entities.ShowMetadata;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchpostQueryServiceImpl implements WatchpostQueryApi {

    private final AnimeRssItemRepository animeItemRepository;
    private final ShowSeriesRepository showSeriesRepository;
    private final WatchpostStatisticsService statisticsService;

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getShowSeriesIdForAnimeItem(Long animeItemId) {
        return animeItemRepository.findShowSeriesIdByAnimeItemId(animeItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeriesSearchResult> getLastSeenSeries(int limit) {
        return showSeriesRepository.findByOrderByLastSeenDesc(Limit.of(limit))
                .stream()
                .limit(limit)
                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeriesSearchResult> searchSeriesByName(String prefix, int limit) {
        return showSeriesRepository.findBySeriesNameContainingIgnoreCase(prefix, Limit.of(limit))
                .stream()
                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeriesSearchResult> searchSeriesBestMatchByName(String prefix, int limit) {
        return showSeriesRepository.findBestMatchSeries(prefix, Limit.of(limit))
                .stream()
                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShowSeriesStateDetailsResult> getSeriesDetailsById(Long id) {
        return showSeriesRepository.findById(id)
                .map(series -> new ShowSeriesStateDetailsResult(
                        series.getId(),
                        series.getSeriesName(),
                        series.getIsInteresting(),
                        series.getIsIgnored(),
                        series.getAutoDownload(),
                        series.getSubmitted(),
                        series.getCustomShareRatio(),
                        series.getLastSeen(),
                        series.getAddedOn()
                ));

//        return showSeriesRepository.findBestMatchSeries(name, Limit.of(1))
//                .stream()
//                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
//                .limit(1)
//                .toList()
//                .getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DownloadDetailsResult> getDownloadDetailsForAnimeItemById(Long animeItemId) {
        return animeItemRepository.findWithSeriesById(animeItemId)
                .map(animeItem -> new DownloadDetailsResult(
                        animeItemId,
                        animeItem.getFileLink(),
                        animeItem.getSeriesName(),
                        animeItem.getEpisode(),
                        animeItem.getShowSeries().getCustomShareRatio()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnimeItemDetailsResult> getLatestAnimeItems(boolean interestingOnly, Pageable pageable) {
        Page<AnimeRssItem> entityPage = animeItemRepository.findAnimeItemsWithDetails(
                interestingOnly,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "localSaveDate"))
                ));

        return entityPage.map(this::mapAnimeItemToResult);
    }

    private AnimeItemDetailsResult mapAnimeItemToResult(AnimeRssItem item) {
        var builder = AnimeItemDetailsResult.builder()
                .animeItemId(item.getId())
                .rawItemName(item.getRawItemName())
                .seriesName(item.getSeriesName())
                .episode(item.getEpisode())
                .fileLink(item.getFileLink())
                .fileSizeBytes(item.getFileSize())
                .videoCategory(item.getVideoCategory())
                .pubDate(item.getPubDate())
                .localSaveDate(item.getLocalSaveDate())
                .downloaded(item.getDownloaded())
                .downloadedOn(item.getDownloadedOn());

        if (item.getRssFeed() != null) {
            builder.sourceFeed(item.getRssFeed().getFeedName());
        }

        if (item.getVideoDetails() != null) {
            builder.resolution(item.getVideoDetails().getResolution())
                    .subtitles(item.getVideoDetails().getSubtitles())
                    .videoSource(item.getVideoDetails().getSource())
                    .sourceType(item.getVideoDetails().getSourceType())
                    .videoType(item.getVideoDetails().getVideoType())
                    .audioType(item.getVideoDetails().getAudioType());
        }

        if (item.getShowSeries() != null) {
            builder.showSeriesId(item.getShowSeries().getId())
                    .isInteresting(Boolean.TRUE.equals(item.getShowSeries().getIsInteresting()))
                    .isIgnored(Boolean.TRUE.equals(item.getShowSeries().getIsIgnored()))
                    .autoDownload(Boolean.TRUE.equals(item.getShowSeries().getAutoDownload()))
                    .customShareRatio(item.getShowSeries().getCustomShareRatio())
                    .malLink(item.getShowSeries().getMalLink());

            if (item.getShowSeries().getMetadata() != null) {
                var metadata = item.getShowSeries().getMetadata();
                builder.coverImageUrl(metadata.getCoverImageUrl())
                        .localizedName(metadata.getLocalizedName())
                        .synopsis(metadata.getSynopsis())
                        .genres(metadata.getGenres())
                        .status(metadata.getStatus())
                        .animeOnlineId(metadata.getAnilistId());
            }
        }

        return builder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowSeriesDetailsFullResult> getLatestShowSeries(boolean unsubmittedOnly, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "addedOn")
        );

        Page<ShowSeries> entityPage = unsubmittedOnly ?
                showSeriesRepository.findAllBySubmittedFalse(pageRequest)
                : showSeriesRepository.findAll(pageRequest);

        return entityPage.map(showSeries -> {
            ShowMetadataFullResult metadataFullResult = null;
            if (showSeries.getMetadata() != null) {
                ShowMetadata metadata = showSeries.getMetadata();
                metadataFullResult = ShowMetadataFullResult.builder()
                        .metadataId(metadata.getId())
                        .malId(metadata.getMalId())
                        .anilistId(metadata.getAnilistId())
                        .localizedName(metadata.getLocalizedName())
                        .coverImageUrl(metadata.getCoverImageUrl())
                        .synopsis(metadata.getSynopsis())
                        .genres(metadata.getGenres())
                        .status(metadata.getStatus())
                        .build();
            }

            return ShowSeriesDetailsFullResult.builder()
                    .showSeriesId(showSeries.getId())
                    .showSeriesName(showSeries.getSeriesName())
                    .interesting(showSeries.getIsInteresting())
                    .ignored(showSeries.getIsIgnored())
                    .autoDownload(showSeries.getAutoDownload())
                    .submitted(showSeries.getSubmitted())
                    .customShareRatio(showSeries.getCustomShareRatio())
                    .lastSeen(showSeries.getLastSeen())
                    .addedOn(showSeries.getAddedOn())
                    .showMetadata(metadataFullResult)
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowSeriesStateDetailsResult> getShowSeriesWithoutMetadata(Pageable pageable) {
        Page<ShowSeries> entityPage = showSeriesRepository.findAllByMetadataNull(PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "addedOn")
        ));

        return entityPage.map(series -> ShowSeriesStateDetailsResult.builder()
                        .id(series.getId())
                        .name(series.getSeriesName())
                        .interesting(series.getIsInteresting())
                        .ignored(series.getIsIgnored())
                        .autoDownload(series.getAutoDownload())
                        .submitted(series.getSubmitted())
                        .customShareRatio(series.getCustomShareRatio())
                        .lastSeen(series.getLastSeen())
                        .addedOn(series.getAddedOn())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public WatchpostStatsResult getStatistics() {
        return statisticsService.calculateStatistics();
    }


}
