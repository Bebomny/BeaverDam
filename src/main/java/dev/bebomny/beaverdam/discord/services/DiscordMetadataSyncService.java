package dev.bebomny.beaverdam.discord.services;

import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class DiscordMetadataSyncService {

    private final JDA jda;
    private final AnalyticsQueryApi analyticsQueryApi;
    private final ApplicationEventPublisher eventPublisher;
    private final Set<Long> monitoredGuilds;

    public DiscordMetadataSyncService(
            JDA jda,
            AnalyticsQueryApi analyticsQueryApi,
            ApplicationEventPublisher eventPublisher,
            @Value("${analytics.discord.monitored_guilds:}") Set<Long> monitoredGuilds) {
        this.jda = jda;
        this.analyticsQueryApi = analyticsQueryApi;
        this.eventPublisher = eventPublisher;
        this.monitoredGuilds = monitoredGuilds != null ? monitoredGuilds : Set.of();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncMetadataOnStartup() {
        Set<Long> syncedUserIds = new HashSet<>();

        for (Long guildId : monitoredGuilds) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                log.atWarn().log("Could not resolve monitored guild {} during startup metadata sync", guildId);
                continue;
            }

            eventPublisher.publishEvent(new DiscordGuildMetadataUpdatedEvent(
                    guild.getIdLong(),
                    guild.getName(),
                    guild.getIconUrl()
            ));

            for (Member member : guild.getMembers()) {
                User user = member.getUser();
                if (syncedUserIds.add(user.getIdLong())) {
                    eventPublisher.publishEvent(new DiscordUserMetadataUpdatedEvent(
                            user.getIdLong(),
                            user.getName(),
                            user.getEffectiveAvatarUrl()
                    ));
                }
            }
        }

        List<Long> missingUserIds = analyticsQueryApi.getUserIdsMissingMetadata();
        List<Long> toBackfill = missingUserIds.stream()
                .filter(id -> !syncedUserIds.contains(id))
                .toList();

        if (!toBackfill.isEmpty()) {
            log.atInfo().log("Backfilling Discord user metadata for {} historical users", toBackfill.size());
            for (Long userId : toBackfill) {
                jda.retrieveUserById(userId).queue(
                        user -> eventPublisher.publishEvent(new DiscordUserMetadataUpdatedEvent(
                                user.getIdLong(),
                                user.getName(),
                                user.getEffectiveAvatarUrl()
                        )),
                        failure -> log.atDebug().log("Failed to backfill Discord metadata for user {}: {}",
                                userId, failure.getMessage())
                );
            }
        }
    }
}
