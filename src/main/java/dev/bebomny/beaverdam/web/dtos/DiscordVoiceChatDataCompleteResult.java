package dev.bebomny.beaverdam.web.dtos;

import lombok.Builder;

@Builder
public record DiscordVoiceChatDataCompleteResult(
        String username, Long userId, String userIconUrl,
        Long totalSecondsSpent,
        Long selfMuteCount, Long selfDeafenCount,
        Long serverMuteCount, Long serverDeafenCount,
        Long streamCount, Long suppressCount
) {
}
