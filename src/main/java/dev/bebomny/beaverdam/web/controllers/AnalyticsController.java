package dev.bebomny.beaverdam.web.controllers;

import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.common.dtos.DiscordVoiceTimeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    //TODO: Add DiscordGuildVoiceTimeResult and pass a list of that to a mapping without and id.
    // This should get data from the .env file for monitored guilds and fetch them and build those objects in the query api
    @GetMapping("/voicetimes/{guildId}")
    public ResponseEntity<List<DiscordVoiceTimeResult>> getVoiceTimes(@PathVariable Long guildId) {
        return ResponseEntity.ok(analyticsQueryApi.getTopVoiceTimeUsers(guildId, 20, false));
    }
}
