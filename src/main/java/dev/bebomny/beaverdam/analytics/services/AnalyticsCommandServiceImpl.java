package dev.bebomny.beaverdam.analytics.services;

import dev.bebomny.beaverdam.analytics.AnalyticsCommandApi;
import dev.bebomny.beaverdam.analytics.api.VoiceEventType;
import dev.bebomny.beaverdam.analytics.api.VoiceStateChangeCommand;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceSession;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceState;
import dev.bebomny.beaverdam.analytics.repos.DiscordGuildRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordUserRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsCommandServiceImpl implements AnalyticsCommandApi {

    private final DiscordVoiceStateRepository voiceStateRepository;
    private final DiscordVoiceSessionRepository voiceSessionRepository;
    private final DiscordGuildRepository discordGuildRepository;
    private final DiscordUserRepository discordUserRepository;

    @Override
    @Async
    @Transactional
    public void recordVoiceStateChange(VoiceStateChangeCommand command) {
        if (command.guildId() != null && command.guildName() != null) {
            discordGuildRepository.upsert(command.guildId(), command.guildName(), command.guildIconUrl());
        }
        if (command.userId() != null && command.username() != null) {
            discordUserRepository.upsert(command.userId(), command.username(), command.userAvatarUrl());
        }

        DiscordVoiceState voiceState = DiscordVoiceState.builder()
                .guildId(command.guildId())
                .userId(command.userId())
                .username(command.username())
                .timestamp(command.timestamp())
                .eventType(command.eventType())
                .actionTrigger(command.actionTrigger())
                .channelId(command.channelId())
                .previousChannelId(command.previousChannelId())
                .stateValue(command.stateValue())
                .build();

        voiceStateRepository.save(voiceState);

        if (command.eventType() == VoiceEventType.CHANNEL_LEAVE || command.eventType() == VoiceEventType.CHANNEL_MOVE) {
            voiceStateRepository.findTopByUserIdAndGuildIdAndEventTypeInOrderByTimestampDesc(
                            command.userId(), command.guildId(),
                            List.of(VoiceEventType.CHANNEL_JOIN, VoiceEventType.CHANNEL_MOVE))
                    .ifPresent(joinEvent -> {
                        long seconds = Duration.between(joinEvent.getTimestamp(), command.timestamp()).toSeconds();

                        if (seconds > 0) {
                            // If CHANNEL_LEAVE, they were in the channel they joined. If CHANNEL_MOVE, use previousChannelId
                            DiscordVoiceSession completedSession = DiscordVoiceSession.builder()
                                    .guildId(command.guildId())
                                    .userId(command.userId())
                                    .username(command.username())
                                    .channelId(command.eventType() == VoiceEventType.CHANNEL_MOVE ? command.previousChannelId() : joinEvent.getChannelId())
                                    .joinedAt(joinEvent.getTimestamp())
                                    .leftAt(command.timestamp())
                                    .durationSeconds(seconds)
                                    .build();
                            voiceSessionRepository.save(completedSession);
                        }
                    });
        }
    }

    @Override
    @Async
    @Transactional
    public void upsertGuildMetadata(Long guildId, String guildName, String iconUrl) {
        if (guildId != null && guildName != null) {
            discordGuildRepository.upsert(guildId, guildName, iconUrl);
        }
    }

    @Override
    @Async
    @Transactional
    public void upsertUserMetadata(Long userId, String username, String avatarUrl) {
        if (userId != null && username != null) {
            discordUserRepository.upsert(userId, username, avatarUrl);
        }
    }
}
