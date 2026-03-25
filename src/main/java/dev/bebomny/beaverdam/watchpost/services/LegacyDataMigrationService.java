package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.common.helpers.AnimeHelper;
import dev.bebomny.beaverdam.common.helpers.FileFormatter;
import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import dev.bebomny.beaverdam.watchpost.entities.RssFeed;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.entities.VideoDetails;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.RssFeedRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import dev.bebomny.beaverdam.watchpost.repos.VideoDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyDataMigrationService {

    private final AnimeRssItemRepository animeItemRepository;
    private final ShowSeriesRepository showSeriesRepository;
    private final VideoDetailsRepository videoDetailsRepository;
    private final RssFeedRepository rssFeedRepository;

    @Value("${legacy.db.url}")
    private String legacyDbUrl;

    @Value("${legacy.db.username}")
    private String legacyDbUser;

    @Value("${legacy.db.password}")
    private String legacyDbPass;

    private JdbcTemplate legacyJdbcTemplate;

    private final AtomicBoolean migrationFinished = new AtomicBoolean(false);

    public boolean isMigrationFinished() {
        return migrationFinished.get();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateLegacyData() {
        if (animeItemRepository.count() > 0) {
            log.atInfo().log("Anime Items Migration Skipped: New tables already contain data");
            migrationFinished.set(true);
            return;
        }

        log.atInfo().log("Connecting to legacy db at {}", legacyDbUrl);

        DriverManagerDataSource legacyDataSource = new DriverManagerDataSource();
        legacyDataSource.setDriverClassName("org.postgresql.Driver");
        legacyDataSource.setUrl(legacyDbUrl);
        legacyDataSource.setUsername(legacyDbUser);
        legacyDataSource.setPassword(legacyDbPass);

        legacyJdbcTemplate = new JdbcTemplate(legacyDataSource);

        try {
            List<Map<String, Object>> legacyItems = legacyJdbcTemplate.queryForList("select * from anime_rss_items");
            Integer showSeriesCount = legacyJdbcTemplate.queryForObject("select count(*) from show_series", Integer.class);
            if (legacyItems.isEmpty()) {
                log.atInfo().log("No legacy data found");
                return;
            }

            log.atInfo().log("Found {} animeItem records and {} different showSeries. Starting migration.", legacyItems.size(), showSeriesCount != null ? showSeriesCount : -1);
            long start = System.currentTimeMillis();

            RssFeed rssFeed = rssFeedRepository.findById(1).orElse(null);
//            int limit = 10;
            for (Map<String, Object> row : legacyItems) {
//                if (limit <= 0) {
//                    break;
//                }

                try {
                    migrateSingleRow(row, rssFeed);
                } catch (Exception e) {
                    log.atError().log("Failed to migrate row Id: {}", row.get("id"), e);
                }

//                limit--;
            }

            long timeElapsed = System.currentTimeMillis() - start;
            log.atInfo().log("Migration finished in {}ms", timeElapsed);
        } catch (Exception e) {
            log.atError().log("Failed to migrate legacy data", e);
        }

        migrationFinished.set(true);
    }

    private void migrateSingleRow(Map<String, Object> row, RssFeed assignedFeed) {
        String oldSeriesName = (String) row.get("video_anime_name");
        if (oldSeriesName == null) oldSeriesName = "Unknown Series";

        String finalOldSeriesName = oldSeriesName;
        ShowSeries series = showSeriesRepository.findBySeriesNameIgnoreCase(oldSeriesName)
                .orElseGet(() -> {
                    Map<String, Object> legacySeries = legacyJdbcTemplate.queryForMap("select * from show_series where name=?", finalOldSeriesName);

                    ShowSeries newSeries = ShowSeries.builder()
                            .seriesName(finalOldSeriesName)
                            .releaseSeason("Unknown")
                            .submitted(true)
                            .isInteresting((Boolean) legacySeries.get("is_interesting"))
                            .isIgnored((Boolean) legacySeries.get("is_ignored"))
                            .autoDownload((Boolean) legacySeries.get("autodownload"))
                            .customShareRatio("2.0")
                            .lastSeen(LocalDateTime.now())
                            .addedOn(parseLegacyDate((String) row.get("local_save_date")))
                            .build();
                    return showSeriesRepository.save(newSeries);
                });

        LocalDateTime pubDate = parseLegacyDate((String) row.get("pub_date"));

        AnimeRssItem newItem = AnimeRssItem.builder()
                .rssFeed(assignedFeed)
                .showSeries(series)
                .rawItemName((String) row.get("title"))
                .seriesName(oldSeriesName)
                .localizedName(oldSeriesName)
                .groupName((String) row.get("video_group_name"))
                .episode((String) row.get("video_episode"))
                .season("Unknown")
                .videoCategory((String) row.get("video_erai_category"))
                .fileLink((String) row.get("link"))
                .fileSize(FileFormatter.parseFileSizeToAmountOfBytes((String) row.get("size")))
                .guid((String) row.get("guid"))
                .pubDate(pubDate)
                .localSaveDate(parseLegacyDate((String) row.get("local_save_date")))
                .infoHash((String) row.get("info_hash"))
                .downloadedOn(parseLegacyDate((String) row.get("local_save_date")))
                .downloaded((Boolean) row.get("downloaded"))
                .displayed((Boolean) row.get("viewed"))
                .repack(((String) row.get("title")).toLowerCase().contains("repack"))
                .build();

        newItem = animeItemRepository.save(newItem);

        VideoDetails details = VideoDetails.builder()
                .animeRssItem(newItem)
                .resolution((String) row.get("video_resolution"))
                .source((String) row.get("video_source"))
                .sourceType((String) row.get("video_source_type"))
                .videoType((String) row.get("video_video_type"))
                .audioType((String) row.get("video_audio_type"))
                .subtitles((String) row.get("video_subtitles"))
                .crc32Checksum((String) row.get("video_crc32checksum"))
                .build();

        videoDetailsRepository.save(details);
    }

    private LocalDateTime parseLegacyDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) return LocalDateTime.now();
        try {
            return AnimeHelper.parseRssPubDate(pubDate);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
