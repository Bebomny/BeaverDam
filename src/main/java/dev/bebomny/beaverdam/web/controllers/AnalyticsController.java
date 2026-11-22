package dev.bebomny.beaverdam.web.controllers;

import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.analytics.api.DiscordGuildVoiceChatDataResult;
import dev.bebomny.beaverdam.analytics.api.DiscordGuildVoiceTimesResult;
import dev.bebomny.beaverdam.analytics.api.DiscordMonitoredGuildResult;
import dev.bebomny.beaverdam.analytics.api.DiscordVoiceTimeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryApi analyticsQueryApi;

    //TODO: split this controller into subsections for different analytics submodules
    // - discord
    // - watchpost
    // - downloader
    // - general

    @GetMapping("/monitoredguilds")
    public ResponseEntity<List<DiscordMonitoredGuildResult>> getMonitoredGuilds() {
        return ResponseEntity.ok(analyticsQueryApi.getMonitoredGuilds());
    }

    @GetMapping("/voicetimes/{guildId}")
    public ResponseEntity<List<DiscordVoiceTimeResult>> getVoiceTimes(@PathVariable Long guildId) {
        return ResponseEntity.ok(analyticsQueryApi.getTopVoiceTimeUsers(guildId, 20, false));
    }

    @GetMapping("/voicetimes/all")
    public ResponseEntity<List<DiscordGuildVoiceTimesResult>> getVoiceTimesForAllMonitoredGuilds() {
        List<DiscordGuildVoiceTimesResult> responseData =
                analyticsQueryApi.getVoiceTimesForAllMonitoredGuilds(20, false);

        if (responseData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/voicedata/{guildId}")
    public ResponseEntity<DiscordGuildVoiceChatDataResult> getVoiceDataForGuildId(
            @PathVariable Long guildId,
            @RequestParam(name = "sort", defaultValue = "total") String sortField,
            @RequestParam(name = "limit", defaultValue = "100") int limit) {

        return analyticsQueryApi.getGuildVoiceChatData(guildId, sortField, limit)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
