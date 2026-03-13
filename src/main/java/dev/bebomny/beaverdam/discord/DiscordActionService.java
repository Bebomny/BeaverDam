package dev.bebomny.beaverdam.discord;

import dev.bebomny.beaverdam.common.events.AnimeItemDisplayedEvent;
import dev.bebomny.beaverdam.common.events.AnimeItemDownloadRequestEvent;
import dev.bebomny.beaverdam.common.events.ShowSeriesMarkAsInterestingEvent;
import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import dev.bebomny.beaverdam.discord.repos.DiscordUiMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Wrapper class for Events as they require a transaction to function properly
@Service
@RequiredArgsConstructor
public class DiscordActionService {

    private final DiscordUiMessageRepository uiMsgRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void saveMappingAndPublishAnimeDisplayed(DiscordUiMessage partialMapping) {
        uiMsgRepository.save(partialMapping);
        eventPublisher.publishEvent(new AnimeItemDisplayedEvent(partialMapping.getTargetItemId()));
    }

    @Transactional
    public void publishButtonDownload(Long targetItemId) {
        eventPublisher.publishEvent(new AnimeItemDownloadRequestEvent(targetItemId));
    }

    @Transactional
    public void publishButtonSetAsInteresting(Long showSeriesId) {
        eventPublisher.publishEvent(new ShowSeriesMarkAsInterestingEvent(showSeriesId));
    }
}
