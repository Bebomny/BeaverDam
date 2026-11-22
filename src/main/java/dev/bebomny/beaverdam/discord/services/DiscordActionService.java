package dev.bebomny.beaverdam.discord.services;

import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import dev.bebomny.beaverdam.discord.repos.DiscordUiMessageRepository;
import dev.bebomny.beaverdam.watchpost.WatchpostCommandApi;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesParam;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesStateDetailsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscordActionService {

    private static final String ANIME_RSS_ITEM_TYPE = "anime_rss_item";

    private final DiscordUiMessageRepository uiMsgRepository;
    private final WatchpostQueryApi watchpostQueryApi;
    private final WatchpostCommandApi watchpostCommandApi;

    @Transactional
    public void removeUiMessageMappingByMessageId(Long messageId) {
        uiMsgRepository.deleteByDiscordMessageId(messageId);
    }

    @Transactional
    public void saveMappingAndPublishAnimeDisplayed(DiscordUiMessage partialMapping) {
        uiMsgRepository.save(partialMapping);
        if (ANIME_RSS_ITEM_TYPE.equals(partialMapping.getTargetItemType())) {
            watchpostCommandApi.markAnimeItemDisplayed(partialMapping.getTargetItemId());
        }
    }

    public boolean publishButtonDownload(Long targetItemId) {
        return watchpostCommandApi.requestAnimeItemDownload(targetItemId);
    }

    @Transactional
    public void publishButtonSetAsInteresting(Long animeItemId) {
        watchpostQueryApi.getShowSeriesIdForAnimeItem(animeItemId).ifPresent(seriesId ->
                watchpostCommandApi.updateShowSeriesParam(seriesId, ShowSeriesParam.INTERESTING, true)
        );
    }

    @Transactional
    public ShowSeriesStateDetailsResult updateShowSeries(Long showSeriesId, ShowSeriesParam showSeriesParam, Boolean newValue) {
//        eventPublisher.publishEvent(new ShowSeriesUpdateParamEvent(showSeriesId, showSeriesParam, newValue));

        return watchpostCommandApi.updateShowSeriesParam(showSeriesId, showSeriesParam, newValue);
    }
}
