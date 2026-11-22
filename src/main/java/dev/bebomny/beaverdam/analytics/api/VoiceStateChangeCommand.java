package dev.bebomny.beaverdam.analytics.api;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VoiceStateChangeCommand(
        Long guildId, String guildName, String guildIconUrl,
        Long userId, String username, String userAvatarUrl,
        LocalDateTime timestamp, VoiceEventType eventType, ActionTrigger actionTrigger,
        Long channelId, Long previousChannelId, Boolean stateValue
) {
}
