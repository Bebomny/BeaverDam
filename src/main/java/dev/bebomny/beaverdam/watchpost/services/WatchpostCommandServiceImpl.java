package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.common.dtos.RssFeedSaveResult;
import dev.bebomny.beaverdam.watchpost.WatchpostCommandApi;
import dev.bebomny.beaverdam.watchpost.entities.RssFeed;
import dev.bebomny.beaverdam.watchpost.repos.RssFeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchpostCommandServiceImpl implements WatchpostCommandApi {

    private final RssFeedRepository rssFeedRepository;

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
}
