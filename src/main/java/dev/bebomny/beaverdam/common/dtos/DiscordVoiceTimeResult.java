package dev.bebomny.beaverdam.common.dtos;

import lombok.Builder;

@Builder
public record DiscordVoiceTimeResult(
        String username, Long userId, Long totalSecondsSpent
) {
}
