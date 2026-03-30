package dev.bebomny.beaverdam.analytics;

import dev.bebomny.beaverdam.common.dtos.DiscordGuildVoiceTimesResult;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceChatDataResult;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceTimeResult;

import java.util.List;
import java.util.Map;

public interface AnalyticsQueryApi {
    List<DiscordVoiceTimeResult> getTopVoiceTimeUsers(Long guildId, int limit, boolean useLegacyRawQuery);

    Map<Long, List<DiscordVoiceTimeResult>> getVoiceTimesForGuildIds(List<Long> guildIds, int limit, boolean useLegacyRawQuery);

    List<DiscordVoiceChatDataResult> getVoiceDataForGuildId(Long guildId, String sortField, int limit);
}
