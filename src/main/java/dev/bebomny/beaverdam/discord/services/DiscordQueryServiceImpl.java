package dev.bebomny.beaverdam.discord.services;

import dev.bebomny.beaverdam.common.dtos.DiscordMonitoredGuildResult;
import dev.bebomny.beaverdam.discord.DiscordQueryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordQueryServiceImpl implements DiscordQueryApi {

    @Value("${analytics.discord.monitored_guilds}")
    private Set<Long> monitoredGuilds;

    private final JDA jda;
    private final DiscordMessagingService discordMessagingService;

    @Override
    public List<DiscordMonitoredGuildResult> getMonitoredGuilds() {
        List<DiscordMonitoredGuildResult> results = new ArrayList<>();

        for (Long monitoredGuildId : monitoredGuilds) {
            Guild guildInstance = discordMessagingService.resolveGuild(monitoredGuildId);

            if (guildInstance == null) {
                log.atWarn().log("Could not resolve guild with id {}. Using Id placeholder", monitoredGuildId);
                results.add(new DiscordMonitoredGuildResult(
                        String.valueOf(monitoredGuildId),
                        monitoredGuildId.toString(),
                        null
                ));

                continue;
            }

            results.add(new DiscordMonitoredGuildResult(
                    guildInstance.getId(),
                    guildInstance.getName(),
                    guildInstance.getIconUrl()
            ));
        }

        return results;
    }

    @Override
    public DiscordMonitoredGuildResult getMonitoredGuildById(Long guildId) {
        if (!monitoredGuilds.contains(guildId)) {
            return null;
        }

        Guild guildInstance = discordMessagingService.resolveGuild(guildId);

        if (guildInstance == null) {
            return new DiscordMonitoredGuildResult(
                    String.valueOf(guildId),
                    guildId.toString(),
                    null
            );
        }

        return new DiscordMonitoredGuildResult(
                guildInstance.getId(),
                guildInstance.getName(),
                guildInstance.getIconUrl()
        );
    }

    @Override
    public Map<Long, String> getIconUrlsByUserIds(List<Long> userIds) {
        Map<Long, String> iconMap = new HashMap<>();

        for (Long id : userIds) {
            User user = jda.getUserById(id);
            if (user != null) {
                iconMap.put(id, user.getEffectiveAvatarUrl());
            } else {
                iconMap.put(id, "https://cdn.discordapp.com/embed/avatars/0.png");
            }
        }

        return iconMap;
    }
}
