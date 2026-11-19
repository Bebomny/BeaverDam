package dev.bebomny.beaverdam.analytics.services;

import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.analytics.entities.DiscordGuild;
import dev.bebomny.beaverdam.analytics.entities.DiscordUser;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceState;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceTimeProjection;
import dev.bebomny.beaverdam.analytics.repos.DiscordGuildRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordUserRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import dev.bebomny.beaverdam.common.dtos.DiscordGuildVoiceChatDataResult;
import dev.bebomny.beaverdam.common.dtos.DiscordGuildVoiceTimesResult;
import dev.bebomny.beaverdam.common.dtos.DiscordMonitoredGuildResult;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceChatDataResult;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceTimeResult;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalyticsQueryApiServiceImpl implements AnalyticsQueryApi {

    static final String DEFAULT_AVATAR_URL = "https://cdn.discordapp.com/embed/avatars/0.png";

    private final DiscordVoiceStateRepository stateRepository;
    private final DiscordVoiceSessionRepository sessionRepository;
    private final DiscordGuildRepository guildRepository;
    private final DiscordUserRepository userRepository;
    private final Set<Long> monitoredGuilds;

    public AnalyticsQueryApiServiceImpl(
            DiscordVoiceStateRepository stateRepository,
            DiscordVoiceSessionRepository sessionRepository,
            DiscordGuildRepository guildRepository,
            DiscordUserRepository userRepository,
            @Value("${analytics.discord.monitored_guilds:}") Set<Long> monitoredGuilds) {
        this.stateRepository = stateRepository;
        this.sessionRepository = sessionRepository;
        this.guildRepository = guildRepository;
        this.userRepository = userRepository;
        this.monitoredGuilds = monitoredGuilds != null ? monitoredGuilds : Set.of();
    }

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
    public List<DiscordMonitoredGuildResult> getMonitoredGuilds() {
        if (monitoredGuilds.isEmpty()) {
            return List.of();
        }

        Map<Long, DiscordGuild> guildsById = guildRepository.findAllById(monitoredGuilds).stream()
                .collect(Collectors.toMap(DiscordGuild::getGuildId, Function.identity()));

        List<DiscordMonitoredGuildResult> results = new ArrayList<>(monitoredGuilds.size());
        for (Long guildId : monitoredGuilds) {
            DiscordGuild guild = guildsById.get(guildId);
            if (guild == null) {
                log.atWarn().log("No persisted metadata found for monitored guild {}. Using ID placeholder", guildId);
                results.add(new DiscordMonitoredGuildResult(
                        String.valueOf(guildId),
                        String.valueOf(guildId),
                        null
                ));
            } else {
                results.add(new DiscordMonitoredGuildResult(
                        String.valueOf(guild.getGuildId()),
                        guild.getName(),
                        guild.getIconUrl()
                ));
            }
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscordGuildVoiceTimesResult> getVoiceTimesForAllMonitoredGuilds(int limit, boolean useLegacyRawQuery) {
        List<DiscordMonitoredGuildResult> guilds = getMonitoredGuilds();
        if (guilds.isEmpty()) {
            return List.of();
        }

        List<Long> guildIds = guilds.stream()
                .map(DiscordMonitoredGuildResult::guildId)
                .map(Long::valueOf)
                .toList();

        Map<Long, List<DiscordVoiceTimeResult>> voiceTimesByGuildId =
                getVoiceTimesForGuildIds(guildIds, limit, useLegacyRawQuery);

        return guilds.stream()
                .map(guild -> {
                    Long id = Long.valueOf(guild.guildId());
                    return new DiscordGuildVoiceTimesResult(
                            id,
                            guild.guildName(),
                            guild.iconUrl(),
                            voiceTimesByGuildId.getOrDefault(id, List.of())
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DiscordGuildVoiceChatDataResult> getGuildVoiceChatData(Long guildId, String sortField, int limit) {
        if (!monitoredGuilds.contains(guildId)) {
            return Optional.empty();
        }

        Optional<DiscordGuild> guildOpt = guildRepository.findById(guildId);
        String guildName = guildOpt.map(DiscordGuild::getName).orElseGet(() -> String.valueOf(guildId));
        String guildIconUrl = guildOpt.map(DiscordGuild::getIconUrl).orElse(null);

        List<DiscordVoiceTimeResult> topVoiceTimes = this.getTopVoiceTimeUsers(guildId, 100, true);
        if (topVoiceTimes.isEmpty()) {
            return Optional.of(new DiscordGuildVoiceChatDataResult(guildId, guildName, guildIconUrl, List.of()));
        }

        List<Long> userIds = topVoiceTimes.stream().map(DiscordVoiceTimeResult::userId).toList();
        Map<Long, DiscordUser> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(DiscordUser::getUserId, Function.identity()));

        List<DiscordVoiceState> userStates = stateRepository.findAllByGuildIdAndStateValueTrue(guildId);

        Map<Long, List<DiscordVoiceState>> statesByUser = userStates.stream()
                .collect(Collectors.groupingBy(DiscordVoiceState::getUserId));

        List<DiscordVoiceChatDataResult> results = topVoiceTimes.stream().map(time -> {
            List<DiscordVoiceState> statesForUser = statesByUser.getOrDefault(time.userId(), List.of());
            DiscordUser userMeta = usersById.get(time.userId());
            String username = (userMeta != null && userMeta.getUsername() != null)
                    ? userMeta.getUsername()
                    : time.username();
            String avatarUrl = (userMeta != null && userMeta.getAvatarUrl() != null)
                    ? userMeta.getAvatarUrl()
                    : DEFAULT_AVATAR_URL;

            return DiscordVoiceChatDataResult.builder()
                    .userId(time.userId())
                    .username(username)
                    .userIconUrl(avatarUrl)
                    .totalSecondsSpent(time.totalSecondsSpent())
                    .selfMuteCount(countEvents(statesForUser, VoiceEventType.SELF_MUTE))
                    .selfDeafenCount(countEvents(statesForUser, VoiceEventType.SELF_DEAFEN))
                    .serverMuteCount(countEvents(statesForUser, VoiceEventType.GUILD_MUTE))
                    .serverDeafenCount(countEvents(statesForUser, VoiceEventType.GUILD_DEAFEN))
                    .streamCount(countEvents(statesForUser, VoiceEventType.STREAM))
                    .suppressCount(countEvents(statesForUser, VoiceEventType.SUPPRESS))
                    .build();
        }).toList();

        List<DiscordVoiceChatDataResult> sorted = sortResults(results, sortField, limit);
        return Optional.of(new DiscordGuildVoiceChatDataResult(guildId, guildName, guildIconUrl, sorted));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserIdsMissingMetadata() {
        return userRepository.findUserIdsMissingMetadata();
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
