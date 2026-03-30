package dev.bebomny.beaverdam.common.dtos;

import lombok.Builder;

@Builder
public record DiscordVoiceChatDataResult(
        String username, Long userId,
        Long totalSecondsSpent,
        Long selfMuteCount, Long selfDeafenCount,
        Long serverMuteCount, Long serverDeafenCount,
        Long streamCount, Long suppressCount
) {
}
