package dev.bebomny.beaverdam.discord.listeners;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DiscordMetadataEventListener extends ListenerAdapter {

    @Value("${analytics.discord.monitored_guilds:}")
    private Set<Long> monitoredGuilds;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        publishGuildUpdateIfMonitored(event.getGuild());
    }

    @Override
    public void onGuildUpdateIcon(GuildUpdateIconEvent event) {
        publishGuildUpdateIfMonitored(event.getGuild());
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        publishUserUpdate(event.getUser());
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        publishUserUpdate(event.getUser());
    }

    private void publishGuildUpdateIfMonitored(Guild guild) {
        if (monitoredGuilds != null && !monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guild.getIdLong())) {
            return;
        }
        eventPublisher.publishEvent(new DiscordGuildMetadataUpdatedEvent(
                guild.getIdLong(),
                guild.getName(),
                guild.getIconUrl()
        ));
    }

    private void publishUserUpdate(User user) {
        eventPublisher.publishEvent(new DiscordUserMetadataUpdatedEvent(
                user.getIdLong(),
                user.getName(),
                user.getEffectiveAvatarUrl()
        ));
    }
}
