package dev.bebomny.beaverdam.common.dtos;

import java.util.List;

public record DiscordGuildVoiceTimesResult(
        Long guildId, String guildName, String iconUrl,
        List<DiscordVoiceTimeResult> voiceTimes
) {
}
