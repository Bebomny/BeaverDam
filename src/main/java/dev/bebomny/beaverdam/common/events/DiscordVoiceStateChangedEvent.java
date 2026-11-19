package dev.bebomny.beaverdam.common.events;

import dev.bebomny.beaverdam.common.events.types.ActionTrigger;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DiscordVoiceStateChangedEvent(
        Long guildId, String guildName, String guildIconUrl,
        Long userId, String username, String userAvatarUrl,
        LocalDateTime timestamp, VoiceEventType eventType, ActionTrigger actionTrigger,
        Long channelId, Long previousChannelId, Boolean stateValue
) {
}
