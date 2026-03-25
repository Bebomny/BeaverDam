package dev.bebomny.beaverdam.analytics.services;

import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceSession;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceState;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import dev.bebomny.beaverdam.common.events.types.ActionTrigger;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegacyAnalyticsMigrationService {

    private final DiscordVoiceStateRepository voiceStateRepository;
    private final DiscordVoiceSessionRepository voiceSessionRepository;

    @Value("${legacy.db.url}")
    private String legacyDbUrl;

    @Value("${legacy.db.username}")
    private String legacyDbUser;

    @Value("${legacy.db.password}")
    private String legacyDbPass;

    private JdbcTemplate legacyJdbcTemplate;
    private final AtomicBoolean migrationFinished = new AtomicBoolean(false);

    public boolean isMigrationFinished() {
        return migrationFinished.get();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateLegacyData() {
        if (voiceStateRepository.count() > 0) {
            log.atInfo().log("Analytics Migration Skipped: New tables already contain data");
            migrationFinished.set(true);
            return;
        }

        log.atInfo().log("Connecting to legacy db at {} for Analytics...", legacyDbUrl);

        DriverManagerDataSource legacyDataSource = new DriverManagerDataSource();
        legacyDataSource.setDriverClassName("org.postgresql.Driver");
        legacyDataSource.setUrl(legacyDbUrl);
        legacyDataSource.setUsername(legacyDbUser);
        legacyDataSource.setPassword(legacyDbPass);

        legacyJdbcTemplate = new JdbcTemplate(legacyDataSource);

        try {
            List<Map<String, Object>> legacyItems = legacyJdbcTemplate.queryForList("SELECT * FROM voice_events ORDER BY timestamp ASC");

            if (legacyItems.isEmpty()) {
                log.atInfo().log("No legacy data found");
                migrationFinished.set(true);
                return;
            }

            log.atInfo().log("Found {} legacy voice events. Starting migration & session calculation...", legacyItems.size());
            long start = System.currentTimeMillis();

            List<DiscordVoiceState> newStates = new ArrayList<>();
            List<DiscordVoiceSession> newSessions = new ArrayList<>();
            Map<Long, DiscordVoiceState> activeUserStates = new HashMap<>();

            for (Map<String, Object> row : legacyItems) {
                try {
                    DiscordVoiceState voiceState = mapRowToState(row);
                    newStates.add(voiceState);

                    Long userId = voiceState.getUserId();
                    VoiceEventType eventType = voiceState.getEventType();

                    if (eventType == VoiceEventType.CHANNEL_JOIN) {
                        activeUserStates.put(userId, voiceState);
                    } else if (eventType == VoiceEventType.CHANNEL_MOVE) {
                        if (activeUserStates.containsKey(userId)) {
                            DiscordVoiceState startState = activeUserStates.get(userId);
                            long seconds = Duration.between(startState.getTimestamp(), voiceState.getTimestamp()).getSeconds();
                            if (seconds > 0) {
                                newSessions.add(buildSession(startState, voiceState, voiceState.getPreviousChannelId(), seconds));
                            }
                        }
                        activeUserStates.put(userId, voiceState);
                    } else if (eventType == VoiceEventType.CHANNEL_LEAVE) {
                        if (activeUserStates.containsKey(userId)) {
                            DiscordVoiceState startState = activeUserStates.get(userId);
                            long seconds = Duration.between(startState.getTimestamp(), voiceState.getTimestamp()).getSeconds();
                            if (seconds > 0) {
                                newSessions.add(buildSession(startState, voiceState, startState.getChannelId(), seconds));
                            }
                            activeUserStates.remove(userId);
                        }
                    }
                } catch (Exception e) {
                    log.atError().log("Failed to migrate row Id: {}", row.get("id"), e);
                }
            }

            log.atInfo().log("Saving {} raw states and {} calculated sessions...", newStates.size(), newSessions.size());

            voiceStateRepository.saveAll(newStates);
            voiceSessionRepository.saveAll(newSessions);

            long timeElapsed = System.currentTimeMillis() - start;
            log.atInfo().log("Analytics Migration finished in {}ms", timeElapsed);
        } catch (Exception e) {
            log.atError().log("Failed to migrate legacy analytics data.", e);
        } finally {
            migrationFinished.set(true);
        }
    }

    private DiscordVoiceState mapRowToState(Map<String, Object> row) {
        Timestamp ts = (Timestamp) row.get("timestamp");

        return DiscordVoiceState.builder()
                .guildId(getLong(row, "guild_id"))
                .userId(getLong(row, "user_id"))
                .username((String) row.get("username"))
                .timestamp(ts != null ? ts.toLocalDateTime() : null)
                .eventType(VoiceEventType.valueOf((String) row.get("event_type")))
                .actionTrigger(ActionTrigger.valueOf((String) row.get("action_trigger")))
                .channelId(getLong(row, "channel_id"))
                .previousChannelId(getLong(row, "previous_channel_id"))
                .stateValue((Boolean) row.get("state_value"))
                .build();
    }

    private DiscordVoiceSession buildSession(DiscordVoiceState start, DiscordVoiceState end, Long targetChannelId, long seconds) {
        return DiscordVoiceSession.builder()
                .guildId(start.getGuildId())
                .userId(start.getUserId())
                .username(start.getUsername())
                .channelId(targetChannelId)
                .joinedAt(start.getTimestamp())
                .leftAt(end.getTimestamp())
                .durationSeconds(seconds)
                .build();
    }

    // Some weird stuff happened with ClassCastExceptions when casting between Long and bigint
    private Long getLong(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }
}
