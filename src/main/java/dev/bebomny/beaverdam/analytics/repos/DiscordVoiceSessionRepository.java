package dev.bebomny.beaverdam.analytics.repos;

import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceSession;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceTimeProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscordVoiceSessionRepository extends JpaRepository<DiscordVoiceSession, Long> {

    @Query("""
                SELECT
                    s.username AS username,
                    s.userId AS userId,
                    SUM(s.durationSeconds) AS totalSeconds
                FROM DiscordVoiceSession s
                WHERE s.guildId = :guildId
                GROUP BY s.username, s.userId
                ORDER BY SUM(s.durationSeconds) DESC
            """)
    List<DiscordVoiceTimeProjection> getVoiceTimesFromSessions(@Param("guildId") Long guildId, PageRequest pageable);
}
