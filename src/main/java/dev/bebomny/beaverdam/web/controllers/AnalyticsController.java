package dev.bebomny.beaverdam.web.controllers;

import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.common.dtos.*;
import dev.bebomny.beaverdam.discord.DiscordQueryApi;
import dev.bebomny.beaverdam.web.dtos.DiscordGuildVoiceChatDataCompleteResult;
import dev.bebomny.beaverdam.web.dtos.DiscordVoiceChatDataCompleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryApi analyticsQueryApi;
    private final DiscordQueryApi discordQueryApi;

    //TODO: split this controller into subsections for different analytics submodules
    // - discord
    // - watchpost
    // - downloader
    // - general

    @GetMapping("/voicetimes/{guildId}")
    public ResponseEntity<List<DiscordVoiceTimeResult>> getVoiceTimes(@PathVariable Long guildId) {
        return ResponseEntity.ok(analyticsQueryApi.getTopVoiceTimeUsers(guildId, 20, false));
    }

    @GetMapping("/voicetimes/all")
    public ResponseEntity<List<DiscordGuildVoiceTimesResult>> getVoiceTimesForAllMonitoredGuilds() {
        List<DiscordMonitoredGuildResult> monitoredGuilds = discordQueryApi.getMonitoredGuilds();

        if (monitoredGuilds.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Long> guildIds = monitoredGuilds.stream()
                .map(DiscordMonitoredGuildResult::guildId)
                .map(Long::valueOf)
                .toList();

        Map<Long, List<DiscordVoiceTimeResult>> voiceTimesByGuildId =
                analyticsQueryApi.getVoiceTimesForGuildIds(guildIds, 20, false);

        List<DiscordGuildVoiceTimesResult> responseData = monitoredGuilds.stream()
                .map(guild -> new DiscordGuildVoiceTimesResult(
                        Long.valueOf(guild.guildId()),
                        guild.guildName(),
                        guild.iconUrl(),
                        voiceTimesByGuildId.getOrDefault(Long.valueOf(guild.guildId()), List.of())
                ))
                .toList();

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/voicedata/{guildId}")
    public ResponseEntity<DiscordGuildVoiceChatDataCompleteResult> getVoiceDataForGuildId(
            @PathVariable Long guildId,
            @RequestParam(name = "sort", defaultValue = "total") String sortField,
            @RequestParam(name = "limit", defaultValue = "100") int limit) {

        DiscordMonitoredGuildResult monitoredGuild = discordQueryApi.getMonitoredGuildById(guildId);
        if (monitoredGuild == null) {
            return ResponseEntity.notFound().build();
        }

        List<DiscordVoiceChatDataResult> rawVoiceChatData =
                analyticsQueryApi.getVoiceDataForGuildId(guildId, sortField, limit);

        List<Long> userIds = rawVoiceChatData.stream().map(DiscordVoiceChatDataResult::userId).toList();

        Map<Long, String> iconUrls = discordQueryApi.getIconUrlsByUserIds(userIds);

        List<DiscordVoiceChatDataCompleteResult> responseData = rawVoiceChatData.stream()
                .map(voiceData ->
                        DiscordVoiceChatDataCompleteResult.builder()
                            .userId(voiceData.userId())
                                .username(voiceData.username())
                                .userIconUrl(iconUrls.get(voiceData.userId()))
                                .totalSecondsSpent(voiceData.totalSecondsSpent())
                                .selfMuteCount(voiceData.selfMuteCount())
                                .selfDeafenCount(voiceData.selfDeafenCount())
                                .serverMuteCount(voiceData.serverMuteCount())
                                .serverDeafenCount(voiceData.serverDeafenCount())
                                .streamCount(voiceData.streamCount())
                                .suppressCount(voiceData.suppressCount())
                                .build()
                )
                .toList();


        return ResponseEntity.ok(new DiscordGuildVoiceChatDataCompleteResult(
                guildId, monitoredGuild.guildName(), monitoredGuild.iconUrl(), responseData
        ));
    }
}
