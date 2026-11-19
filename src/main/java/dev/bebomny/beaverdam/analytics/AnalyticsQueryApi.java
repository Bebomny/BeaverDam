package dev.bebomny.beaverdam.analytics;

import dev.bebomny.beaverdam.common.dtos.DiscordGuildVoiceChatDataResult;
import dev.bebomny.beaverdam.common.dtos.DiscordGuildVoiceTimesResult;
import dev.bebomny.beaverdam.common.dtos.DiscordMonitoredGuildResult;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceTimeResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AnalyticsQueryApi {
    List<DiscordVoiceTimeResult> getTopVoiceTimeUsers(Long guildId, int limit, boolean useLegacyRawQuery);

    Map<Long, List<DiscordVoiceTimeResult>> getVoiceTimesForGuildIds(List<Long> guildIds, int limit, boolean useLegacyRawQuery);

    List<DiscordMonitoredGuildResult> getMonitoredGuilds();

    List<DiscordGuildVoiceTimesResult> getVoiceTimesForAllMonitoredGuilds(int limit, boolean useLegacyRawQuery);

    Optional<DiscordGuildVoiceChatDataResult> getGuildVoiceChatData(Long guildId, String sortField, int limit);

    List<Long> getUserIdsMissingMetadata();
}
