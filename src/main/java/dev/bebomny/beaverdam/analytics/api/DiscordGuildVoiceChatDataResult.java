package dev.bebomny.beaverdam.analytics.api;

import java.util.List;

public record DiscordGuildVoiceChatDataResult(
        Long guildId, String guildName, String iconUrl,
        List<DiscordVoiceChatDataResult> voiceChatData
) {
}
