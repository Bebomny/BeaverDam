package dev.bebomny.beaverdam.analytics;

import dev.bebomny.beaverdam.analytics.api.VoiceStateChangeCommand;

public interface AnalyticsCommandApi {
    void recordVoiceStateChange(VoiceStateChangeCommand command);

    void upsertGuildMetadata(Long guildId, String guildName, String iconUrl);

    void upsertUserMetadata(Long userId, String username, String avatarUrl);
}
