package dev.bebomny.beaverdam.web.dtos;

import java.util.List;

public record DiscordGuildVoiceChatDataCompleteResult(
        Long guildId, String guildName, String iconUrl,
        List<DiscordVoiceChatDataCompleteResult> voiceChatData

) {
}
