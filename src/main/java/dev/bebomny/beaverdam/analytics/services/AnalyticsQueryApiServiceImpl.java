package dev.bebomny.beaverdam.analytics.services;

import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceTimeProjection;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceTimeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsQueryApiServiceImpl implements AnalyticsQueryApi {

    private final DiscordVoiceStateRepository stateRepository;
    private final DiscordVoiceSessionRepository sessionRepository;

    @Override
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
}
