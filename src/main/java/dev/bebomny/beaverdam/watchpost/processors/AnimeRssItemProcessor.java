package dev.bebomny.beaverdam.watchpost.processors;

import com.apptasticsoftware.rssreader.Item;
import dev.bebomny.beaverdam.common.events.WatchpostNewAnimeItemEvent;
import dev.bebomny.beaverdam.common.events.WatchpostNewShowSeriesEvent;
import dev.bebomny.beaverdam.common.helpers.AnimeHelper;
import dev.bebomny.beaverdam.watchpost.dto.AnimeMetadata;
import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import dev.bebomny.beaverdam.watchpost.entities.RssFeed;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.entities.VideoDetails;
import dev.bebomny.beaverdam.watchpost.parsers.AnimeMetadataParser;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.RssFeedRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import dev.bebomny.beaverdam.watchpost.repos.VideoDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
public abstract class AnimeRssItemProcessor<T extends Item> implements ContentProcessor<T> {

    protected final AnimeRssItemRepository itemRepository;
    protected final ShowSeriesRepository showSeriesRepository;
    protected final VideoDetailsRepository videoDetailsRepository;
    protected final RssFeedRepository rssFeedRepository;
    protected final ApplicationEventPublisher eventPublisher;

    private final AnimeMetadataParser<T> metadataParser;

    @Value("${watchpost.showseries.default_share_ratio}")
    private String defaultShareRatio;

    @Override
    @Transactional
    public void process(T rssItem, Integer sourceId) {
        String infoHash = metadataParser.parseInfoHash(rssItem);

        if (itemRepository.existsByInfoHash(infoHash)) {
            return;
        }

        AnimeMetadata metadata = metadataParser.parseMetadata(rssItem);

        ShowSeries series = showSeriesRepository.findBySeriesNameIgnoreCase(metadata.seriesName())
                .orElseGet(() -> {
                    ShowSeries newShowSeries = showSeriesRepository.save(
                            ShowSeries.builder()
                                    .seriesName(metadata.seriesName())
                                    .releaseSeason(AnimeHelper.getReleaseSeasonForDate(metadata.pubDate()))
                                    .submitted(false)
                                    .isInteresting(false)
                                    .isIgnored(false)
                                    .autoDownload(false)
                                    .customShareRatio(defaultShareRatio)
                                    .lastSeen(LocalDateTime.now())
                                    .addedOn(LocalDateTime.now())
                                    .build());

                    eventPublisher.publishEvent(new WatchpostNewShowSeriesEvent(newShowSeries.getId()));

                    return newShowSeries;
                });

        RssFeed sourceFeed = rssFeedRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalStateException("Source Feed missing: " + sourceId));

        AnimeRssItem newAnimeItem = AnimeRssItem.builder()
                .rssFeed(sourceFeed)
                .showSeries(series)
//                .internalFileId()
                .rawItemName(metadata.rawTitle())
                .seriesName(metadata.seriesName())
                .localizedName(metadata.seriesName()) //TODO: change after adding online anime matching support
                .groupName(metadata.groupName())
                .episode(metadata.episode())
                .season(metadata.season())
//                .rssCategory()
                .videoCategory(metadata.videoCategory())
                .fileLink(metadata.fileLink())
                .fileSize(metadata.fileSizeBytes())
                .guid(metadata.guid())
//                .onlineId()
                .pubDate(metadata.pubDate())
                .localSaveDate(LocalDateTime.now())
                .infoHash(infoHash)
                .downloadedOn(null)
                .downloaded(false)
                .displayed(false)
                .repack(metadata.repack())
                .build();

        itemRepository.save(newAnimeItem);

        VideoDetails newVideoDetails = VideoDetails.builder()
                .animeRssItem(newAnimeItem)
                .resolution(metadata.resolution())
                .source(metadata.source())
                .sourceType(metadata.sourceType())
                .videoType(metadata.videoType())
                .audioType(metadata.audioType())
                .subtitles(metadata.subtitles())
                .crc32Checksum(metadata.crc32Checksum())
                .build();

        videoDetailsRepository.save(newVideoDetails);

        //update series
        series.setLastSeen(LocalDateTime.now());

        eventPublisher.publishEvent(WatchpostNewAnimeItemEvent.builder()
                .animeItemId(newAnimeItem.getId())
                .rawItemName(newAnimeItem.getRawItemName())
                .seriesName(newAnimeItem.getSeriesName())
                .episode(newAnimeItem.getEpisode())
                .sourceFeed(sourceFeed.getFeedName())
                .fileLink(newAnimeItem.getFileLink())
                .resolution(newVideoDetails.getResolution())
                .subtitles(newVideoDetails.getSubtitles())
                .fileSizeBytes(newAnimeItem.getFileSize())
                .videoCategory(newAnimeItem.getVideoCategory())
                .videoSource(newVideoDetails.getSource())
                .sourceType(newVideoDetails.getSourceType())
                .videoType(newVideoDetails.getVideoType())
                .audioType(newVideoDetails.getAudioType())
                .showSeriesId(series.getId())
                .isInteresting(series.getIsInteresting())
                .isIgnored(series.getIsIgnored())
                .autoDownload(series.getAutoDownload())
                .customShareRatio(series.getCustomShareRatio())
                .malLink(series.getMalLink())
                .build()
        );
    }
}
