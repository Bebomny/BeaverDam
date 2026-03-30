package dev.bebomny.beaverdam.web.controllers;

import dev.bebomny.beaverdam.common.dtos.DiscordMonitoredGuildResult;
import dev.bebomny.beaverdam.discord.DiscordQueryApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/discord")
@RequiredArgsConstructor
public class DiscordController {

    private final DiscordQueryApi discordQueryApi;

    @GetMapping("/monitoredguilds")
    public ResponseEntity<List<DiscordMonitoredGuildResult>> getMonitoredGuilds() {
        return ResponseEntity.ok(discordQueryApi.getMonitoredGuilds());
    }
}
