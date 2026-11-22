package dev.bebomny.beaverdam.analytics.api;

import java.util.List;

public record DiscordGuildVoiceTimesResult(
        Long guildId, String guildName, String iconUrl,
        List<DiscordVoiceTimeResult> voiceTimes
) {
}
