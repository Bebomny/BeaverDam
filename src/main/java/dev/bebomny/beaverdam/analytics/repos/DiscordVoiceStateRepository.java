package dev.bebomny.beaverdam.analytics.repos;

import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceState;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceTimeProjection;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DiscordVoiceStateRepository extends JpaRepository<DiscordVoiceState, Long> {
    @Query(value = """
        WITH SessionData AS (
            SELECT
                user_id,
                username,
                timestamp AS event_time,
                event_type,
                LEAD(timestamp) OVER (PARTITION BY user_id ORDER BY timestamp ASC) AS next_event_time,
                LEAD(event_type) OVER (PARTITION BY user_id ORDER BY timestamp ASC) AS next_event_type
            FROM analytics.discord_voice_states
            WHERE guild_id = :guildId
              AND event_type IN ('CHANNEL_JOIN', 'CHANNEL_LEAVE') 
        ),
        AggregatedSeconds AS (
            SELECT
                user_id,
                MAX(username) AS username,
                COALESCE(SUM(EXTRACT(EPOCH FROM (next_event_time - event_time))), 0) AS total_seconds
            FROM SessionData
            WHERE event_type = 'CHANNEL_JOIN'
              AND next_event_type = 'CHANNEL_LEAVE'
            GROUP BY user_id
        )
        SELECT 
            username AS username,
            user_id,
            CAST(total_seconds AS BIGINT) AS totalSeconds
        FROM AggregatedSeconds
        ORDER BY total_seconds DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<DiscordVoiceTimeProjection> getVoiceTimesFromRawStates(@Param("guildId") Long guildId, @Param("limit") int limit);

    Optional<DiscordVoiceState> findTopByUserIdAndGuildIdAndEventTypeInOrderByTimestampDesc(Long userId, Long guildId, Collection<VoiceEventType> eventTypes);

    List<DiscordVoiceState> findAllByGuildIdAndStateValueTrue(Long guildId);
}
