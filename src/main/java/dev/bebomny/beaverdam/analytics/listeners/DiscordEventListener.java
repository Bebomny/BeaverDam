package dev.bebomny.beaverdam.analytics.listeners;

import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceSession;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceState;
import dev.bebomny.beaverdam.analytics.repos.DiscordGuildRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordUserRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import dev.bebomny.beaverdam.common.events.DiscordGuildMetadataUpdatedEvent;
import dev.bebomny.beaverdam.common.events.DiscordUserMetadataUpdatedEvent;
import dev.bebomny.beaverdam.common.events.DiscordVoiceStateChangedEvent;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscordEventListener {

    private final DiscordVoiceStateRepository voiceStateRepository;
    private final DiscordVoiceSessionRepository voiceSessionRepository;
    private final DiscordGuildRepository discordGuildRepository;
    private final DiscordUserRepository discordUserRepository;

    @EventListener
    @Async
    @Transactional
    public void onDiscordVoiceStateChangedEvent(DiscordVoiceStateChangedEvent event) {
        if (event.guildId() != null && event.guildName() != null) {
            discordGuildRepository.upsert(event.guildId(), event.guildName(), event.guildIconUrl());
        }
        if (event.userId() != null && event.username() != null) {
            discordUserRepository.upsert(event.userId(), event.username(), event.userAvatarUrl());
        }

        DiscordVoiceState voiceState = DiscordVoiceState.builder()
                .guildId(event.guildId())
                .userId(event.userId())
                .username(event.username())
                .timestamp(event.timestamp())
                .eventType(event.eventType())
                .actionTrigger(event.actionTrigger())
                .channelId(event.channelId())
                .previousChannelId(event.previousChannelId())
                .stateValue(event.stateValue())
                .build();

        voiceStateRepository.save(voiceState);

        if (event.eventType() == VoiceEventType.CHANNEL_LEAVE || event.eventType() == VoiceEventType.CHANNEL_MOVE) {
            voiceStateRepository.findTopByUserIdAndGuildIdAndEventTypeInOrderByTimestampDesc(
                            event.userId(), event.guildId(),
                            List.of(VoiceEventType.CHANNEL_JOIN, VoiceEventType.CHANNEL_MOVE))
                    .ifPresent(joinEvent -> {
                        long seconds = Duration.between(joinEvent.getTimestamp(), event.timestamp()).toSeconds();

                        if (seconds > 0) {
                            // If CHANNEL_LEAVE, they were in the channel they joined. If CHANNEL_MOVE, use previousChannelId
                            DiscordVoiceSession completedSession = DiscordVoiceSession.builder()
                                    .guildId(event.guildId())
                                    .userId(event.userId())
                                    .username(event.username())
                                    .channelId(event.eventType() == VoiceEventType.CHANNEL_MOVE ? event.previousChannelId() : joinEvent.getChannelId())
                                    .joinedAt(joinEvent.getTimestamp())
                                    .leftAt(event.timestamp())
                                    .durationSeconds(seconds)
                                    .build();
                            voiceSessionRepository.save(completedSession);
                        }
                    });
        }
    }

    @EventListener
    @Async
    @Transactional
    public void onDiscordGuildMetadataUpdated(DiscordGuildMetadataUpdatedEvent event) {
        if (event.guildId() != null && event.guildName() != null) {
            discordGuildRepository.upsert(event.guildId(), event.guildName(), event.iconUrl());
        }
    }

    @EventListener
    @Async
    @Transactional
    public void onDiscordUserMetadataUpdated(DiscordUserMetadataUpdatedEvent event) {
        if (event.userId() != null && event.username() != null) {
            discordUserRepository.upsert(event.userId(), event.username(), event.avatarUrl());
        }
    }
}
