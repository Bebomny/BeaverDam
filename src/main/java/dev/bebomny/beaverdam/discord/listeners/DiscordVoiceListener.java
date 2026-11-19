package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.common.events.DiscordVoiceStateChangedEvent;
import dev.bebomny.beaverdam.common.events.types.ActionTrigger;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DiscordVoiceListener extends ListenerAdapter {

    @Value("${analytics.discord.monitored_guilds:}")
    private Set<Long> monitoredGuilds;

    private final ApplicationEventPublisher eventPublisher;

    private boolean isNotMonitored(Guild guild) {
        return monitoredGuilds != null && !monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guild.getIdLong());
    }

    private DiscordVoiceStateChangedEvent.DiscordVoiceStateChangedEventBuilder baseEvent(Guild guild, User user) {
        return DiscordVoiceStateChangedEvent.builder()
                .guildId(guild.getIdLong())
                .guildName(guild.getName())
                .guildIconUrl(guild.getIconUrl())
                .userId(user.getIdLong())
                .username(user.getName())
                .userAvatarUrl(user.getEffectiveAvatarUrl())
                .actionTrigger(ActionTrigger.SELF)
                .timestamp(LocalDateTime.now());
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        if (isNotMonitored(guild)) {
            return;
        }

        User user = event.getEntity().getUser();
        AudioChannelUnion joinedChannel = event.getChannelJoined();
        AudioChannelUnion leftChannel = event.getChannelLeft();

        if (joinedChannel != null && leftChannel == null) {
            // the member joined an audio channel
            eventPublisher.publishEvent(baseEvent(guild, user)
                    .eventType(VoiceEventType.CHANNEL_JOIN)
                    .channelId(joinedChannel.getIdLong())
                    .build());
        }

        if (leftChannel != null && joinedChannel == null) {
            // the member left an audio channel
            eventPublisher.publishEvent(baseEvent(guild, user)
                    .eventType(VoiceEventType.CHANNEL_LEAVE)
                    .channelId(leftChannel.getIdLong())
                    .build());
        }

        if (joinedChannel != null && leftChannel != null) {
            // the member moved between two audio channels in the same guild
            eventPublisher.publishEvent(baseEvent(guild, user)
                    .eventType(VoiceEventType.CHANNEL_MOVE)
                    .channelId(joinedChannel.getIdLong())
                    .previousChannelId(leftChannel.getIdLong())
                    .build());
        }
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        Guild guild = event.getGuild();
        if (isNotMonitored(guild)) {
            return;
        }

        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        eventPublisher.publishEvent(baseEvent(guild, event.getMember().getUser())
                .eventType(VoiceEventType.GUILD_MUTE)
                .channelId(channelId)
                .stateValue(event.isGuildMuted())
                .build());
    }

    @Override
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {
        Guild guild = event.getGuild();
        if (isNotMonitored(guild)) {
            return;
        }

        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        eventPublisher.publishEvent(baseEvent(guild, event.getMember().getUser())
                .eventType(VoiceEventType.GUILD_DEAFEN)
                .channelId(channelId)
                .stateValue(event.isGuildDeafened())
                .build());
    }

    @Override
    public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
        Guild guild = event.getGuild();
        if (isNotMonitored(guild)) {
            return;
        }

        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        eventPublisher.publishEvent(baseEvent(guild, event.getMember().getUser())
                .eventType(VoiceEventType.SELF_MUTE)
                .channelId(channelId)
                .stateValue(event.isSelfMuted())
                .build());
    }

    @Override
    public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {
        Guild guild = event.getGuild();
        if (isNotMonitored(guild)) {
            return;
        }

        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        eventPublisher.publishEvent(baseEvent(guild, event.getMember().getUser())
                .eventType(VoiceEventType.SELF_DEAFEN)
                .channelId(channelId)
                .stateValue(event.isSelfDeafened())
                .build());
    }

    @Override
    public void onGuildVoiceSuppress(GuildVoiceSuppressEvent event) {
        Guild guild = event.getGuild();
        if (isNotMonitored(guild)) {
            return;
        }

        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        eventPublisher.publishEvent(baseEvent(guild, event.getMember().getUser())
                .eventType(VoiceEventType.SUPPRESS)
                .channelId(channelId)
                .stateValue(event.isSuppressed())
                .build());
    }

    @Override
    public void onGuildVoiceStream(GuildVoiceStreamEvent event) {
        Guild guild = event.getGuild();
        if (isNotMonitored(guild)) {
            return;
        }

        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        eventPublisher.publishEvent(baseEvent(guild, event.getMember().getUser())
                .eventType(VoiceEventType.STREAM)
                .channelId(channelId)
                .stateValue(event.isStream())
                .build());
    }

    @Override
    public void onGuildVoiceVideo(GuildVoiceVideoEvent event) {
        Guild guild = event.getGuild();
        if (isNotMonitored(guild)) {
            return;
        }

        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        eventPublisher.publishEvent(baseEvent(guild, event.getMember().getUser())
                .eventType(VoiceEventType.VIDEO)
                .channelId(channelId)
                .stateValue(event.isSendingVideo())
                .build());
    }
}
