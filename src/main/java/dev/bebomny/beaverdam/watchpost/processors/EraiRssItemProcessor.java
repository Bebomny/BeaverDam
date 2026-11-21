package dev.bebomny.beaverdam.watchpost.processors;

import dev.bebomny.beaverdam.watchpost.WatchpostCommandApi;
import dev.bebomny.beaverdam.watchpost.dto.EraiRssReaderItem;
import dev.bebomny.beaverdam.watchpost.parsers.EraiMetadataParser;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.RssFeedRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import dev.bebomny.beaverdam.watchpost.repos.VideoDetailsRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EraiRssItemProcessor extends AnimeRssItemProcessor<EraiRssReaderItem> {

    public EraiRssItemProcessor(
           AnimeRssItemRepository itemRepo,
           ShowSeriesRepository showSeriesRepo,
           VideoDetailsRepository videoDetailsRepo,
           RssFeedRepository rssFeedRepo,
           ApplicationEventPublisher eventPublisher,
           WatchpostCommandApi watchpostCommandApi,
           EraiMetadataParser eraiParser) {
        super(itemRepo, showSeriesRepo, videoDetailsRepo, rssFeedRepo, eventPublisher, watchpostCommandApi, eraiParser);
    }

    @Override
    public boolean supports(String sourceType) {
        return "ERAI_RSS".equalsIgnoreCase(sourceType);
    }
}
