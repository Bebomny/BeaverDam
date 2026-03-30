package dev.bebomny.beaverdam.analytics.services;

import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceState;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceTimeProjection;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceChatDataResult;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceTimeResult;
import dev.bebomny.beaverdam.common.events.types.ActionTrigger;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsQueryApiServiceImpl implements AnalyticsQueryApi {

    private final DiscordVoiceStateRepository stateRepository;
    private final DiscordVoiceSessionRepository sessionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DiscordVoiceTimeResult> getTopVoiceTimeUsers(Long guildId, int limit, boolean useLegacyRawQuery) {
        List<DiscordVoiceTimeProjection> results;

        if (useLegacyRawQuery) {
            results = stateRepository.getVoiceTimesFromRawStates(guildId, limit);
        } else {
            results = sessionRepository.getVoiceTimesFromSessions(guildId, PageRequest.of(0, limit));
        }

        return results.stream()
                .map(proj ->
                        new DiscordVoiceTimeResult(
                                proj.getUsername(), proj.getUserId(), proj.getTotalSeconds()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<DiscordVoiceTimeResult>> getVoiceTimesForGuildIds(List<Long> guildIds, int limit, boolean useLegacyRawQuery) {
        Map<Long, List<DiscordVoiceTimeResult>> results = new ConcurrentHashMap<>();

        for (Long guildId : guildIds) {
            results.put(guildId, this.getTopVoiceTimeUsers(guildId, limit, useLegacyRawQuery));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscordVoiceChatDataResult> getVoiceDataForGuildId(Long guildId, String sortField, int limit) {
        List<DiscordVoiceTimeResult> topVoiceTimes = this.getTopVoiceTimeUsers(guildId, 100, true);

        List<Long> userIds = topVoiceTimes.stream().map(DiscordVoiceTimeResult::userId).toList();

        if (userIds.isEmpty()) {
            return List.of();
        }

        List<DiscordVoiceState> userStates = stateRepository.findAllByGuildIdAndStateValueTrue(guildId);

        Map<Long, List<DiscordVoiceState>> statesByUser = userStates.stream()
                .collect(Collectors.groupingBy(DiscordVoiceState::getUserId));

        List<DiscordVoiceChatDataResult> results = topVoiceTimes.stream().map(time -> {
            List<DiscordVoiceState> statesForUser = statesByUser.getOrDefault(time.userId(), List.of());

            return DiscordVoiceChatDataResult.builder()
                    .userId(time.userId())
                    .username(time.username())
                    .totalSecondsSpent(time.totalSecondsSpent())
                    .selfMuteCount(countEvents(statesForUser, VoiceEventType.SELF_MUTE))
                    .selfDeafenCount(countEvents(statesForUser, VoiceEventType.SELF_DEAFEN))
                    .serverMuteCount(countEvents(statesForUser, VoiceEventType.GUILD_MUTE))
                    .serverDeafenCount(countEvents(statesForUser, VoiceEventType.GUILD_DEAFEN))
                    .streamCount(countEvents(statesForUser, VoiceEventType.STREAM))
                    .suppressCount(countEvents(statesForUser, VoiceEventType.SUPPRESS))
                    .build();
        }).toList();

        return sortResults(results, sortField, limit);
    }

    private long countEvents(List<DiscordVoiceState> states, VoiceEventType type) {
        return states.stream()
                .filter(state -> state.getEventType().equals(type))
                .count();
    }

    private List<DiscordVoiceChatDataResult> sortResults(List<DiscordVoiceChatDataResult> results, String sortField, int limit) {
        Comparator<DiscordVoiceChatDataResult> comparator = switch (sortField.toLowerCase()) {
            case "stream" -> Comparator.comparing(DiscordVoiceChatDataResult::streamCount);
            case "suppress" -> Comparator.comparing(DiscordVoiceChatDataResult::suppressCount);
            case "self_mute" -> Comparator.comparing(DiscordVoiceChatDataResult::selfMuteCount);
            case "self_deafen" -> Comparator.comparing(DiscordVoiceChatDataResult::selfDeafenCount);
            case "server_mute" -> Comparator.comparing(DiscordVoiceChatDataResult::serverMuteCount);
            case "server_deafen" -> Comparator.comparing(DiscordVoiceChatDataResult::serverDeafenCount);
            default -> Comparator.comparing(DiscordVoiceChatDataResult::totalSecondsSpent);
        };

        return results.stream()
                .sorted(comparator.reversed())
                .limit(limit)
                .toList();
    }
}
