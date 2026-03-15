package dev.bebomny.beaverdam.watchpost;

import com.apptasticsoftware.rssreader.RssReader;
import dev.bebomny.beaverdam.watchpost.entities.RssFeed;
import dev.bebomny.beaverdam.watchpost.readers.EraiRssReader;
import dev.bebomny.beaverdam.watchpost.repos.RssFeedRepository;
import dev.bebomny.beaverdam.watchpost.services.LegacyDataMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchpostScheduler {

    private final RssFeedRepository rssFeedRepository;
    private final WatchpostItemManager itemManager;
    private final HttpClient vpnHttpClient;
    private final LegacyDataMigrationService migrationService;

    @Scheduled(fixedDelayString = "${watchpost.showseries.poll_interval:1800000}")
    public void executeFetchCycle() {
        if (!migrationService.isMigrationFinished()) {
            log.atInfo().log("Migration is currently running, will wait until next cycle...");
            return;
        }

        List<RssFeed> activeRssFeeds = rssFeedRepository.findByEnabledTrue();

        if (activeRssFeeds.isEmpty()) {
            log.atDebug().log("No active RSS feed found");
            return;
        }

        for (RssFeed feed : activeRssFeeds) {
            try {
                log.atInfo().log("Fetching feed: {}", feed.getFeedName());
                String sourceType = feed.getRssReaderName();

                if ("ERAI_RSS".equalsIgnoreCase(sourceType)) {
                    EraiRssReader eraiRssReader = new EraiRssReader(vpnHttpClient);

                    eraiRssReader.read(feed.getFeedUrl())
                            .forEach(item -> itemManager.routeAndProcess(item, sourceType, feed.getId()));
                } else {
                    RssReader standardReader = new RssReader(vpnHttpClient);

                    standardReader.read(feed.getFeedUrl())
                            .forEach(item -> itemManager.routeAndProcess(item, sourceType, feed.getId()));
                }

                feed.setLastSuccessfulFetch(LocalDateTime.now());
                feed.setLastErrorMessage(null);
                rssFeedRepository.save(feed);
            } catch (Exception e) {
                log.error("Network or Parsing error while fetching feed: {}", feed.getFeedName(), e);

                feed.setLastErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
                rssFeedRepository.save(feed);
            }
        }
    }
}
