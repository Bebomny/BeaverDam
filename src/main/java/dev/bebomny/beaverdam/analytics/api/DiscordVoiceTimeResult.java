package dev.bebomny.beaverdam.analytics.api;

import lombok.Builder;

@Builder
public record DiscordVoiceTimeResult(
        String username, Long userId, Long totalSecondsSpent
) {
}
