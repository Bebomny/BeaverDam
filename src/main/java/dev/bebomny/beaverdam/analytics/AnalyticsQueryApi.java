package dev.bebomny.beaverdam.analytics;

import dev.bebomny.beaverdam.common.dtos.DiscordVoiceTimeResult;

import java.util.List;

public interface AnalyticsQueryApi {
    List<DiscordVoiceTimeResult> getTopVoiceTimeUsers(Long guildId, int limit, boolean useLegacyRawQuery);
}
