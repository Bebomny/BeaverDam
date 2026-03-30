package dev.bebomny.beaverdam.common.dtos;

import java.util.List;

public record DiscordGuildVoiceChatDataResult(
        Long guildId, String guildName, String iconUrl,
        List<DiscordVoiceChatDataResult> voiceChatData
) {
}
