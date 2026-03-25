package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.common.events.DiscordVoiceStateChangedEvent;
import dev.bebomny.beaverdam.common.events.types.ActionTrigger;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import lombok.RequiredArgsConstructor;
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

    @Value("${analytics.discord.monitored_guilds}")
    private Set<Long> monitoredGuilds;

    private final ApplicationEventPublisher eventPublisher;


    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guildId)) {
            return;
        }

        String username = event.getEntity().getUser().getName();
        long userId = event.getEntity().getUser().getIdLong();

        AudioChannelUnion joinedChannel = event.getChannelJoined();
        AudioChannelUnion leftChannel = event.getChannelLeft();

        if (joinedChannel != null && leftChannel == null) {
            // the member joined an audio channel
            DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                    .guildId(guildId)
                    .userId(userId)
                    .username(username)
                    .eventType(VoiceEventType.CHANNEL_JOIN)
                    .actionTrigger(ActionTrigger.SELF)
                    .timestamp(LocalDateTime.now())
                    .channelId(joinedChannel.getIdLong())
                    .build();
            eventPublisher.publishEvent(voiceEvent);
        }

        if (leftChannel != null && joinedChannel == null) {
            // the member left an audio channel
            DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                    .guildId(event.getGuild().getIdLong())
                    .userId(userId)
                    .username(username)
                    .eventType(VoiceEventType.CHANNEL_LEAVE)
                    .actionTrigger(ActionTrigger.SELF)
                    .timestamp(LocalDateTime.now())
                    .channelId(leftChannel.getIdLong())
                    .build();
            eventPublisher.publishEvent(voiceEvent);
        }

        if (joinedChannel != null && leftChannel != null) {
            // the member moved between two audio channels in the same guild
            DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                    .guildId(event.getGuild().getIdLong())
                    .userId(userId)
                    .username(username)
                    .eventType(VoiceEventType.CHANNEL_MOVE)
                    .actionTrigger(ActionTrigger.SELF)
                    .timestamp(LocalDateTime.now())
                    .channelId(joinedChannel.getIdLong())
                    .previousChannelId(leftChannel.getIdLong())
                    .build();
            eventPublisher.publishEvent(voiceEvent);
        }
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guildId)) {
            return;
        }

        String username = event.getMember().getUser().getName();
        long userId = event.getMember().getUser().getIdLong();
        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                .guildId(event.getGuild().getIdLong())
                .userId(userId)
                .username(username)
                .eventType(VoiceEventType.GUILD_MUTE)
                .actionTrigger(ActionTrigger.SELF)
                .timestamp(LocalDateTime.now())
                .channelId(channelId)
                .stateValue(event.isGuildMuted())
                .build();
        eventPublisher.publishEvent(voiceEvent);
    }

    @Override
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guildId)) {
            return;
        }

        String username = event.getMember().getUser().getName();
        long userId = event.getMember().getUser().getIdLong();
        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                .guildId(event.getGuild().getIdLong())
                .userId(userId)
                .username(username)
                .eventType(VoiceEventType.GUILD_DEAFEN)
                .actionTrigger(ActionTrigger.SELF)
                .timestamp(LocalDateTime.now())
                .channelId(channelId)
                .stateValue(event.isGuildDeafened())
                .build();
        eventPublisher.publishEvent(voiceEvent);
    }

    @Override
    public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guildId)) {
            return;
        }

        String username = event.getMember().getUser().getName();
        long userId = event.getMember().getUser().getIdLong();
        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                .guildId(event.getGuild().getIdLong())
                .userId(userId)
                .username(username)
                .eventType(VoiceEventType.SELF_MUTE)
                .actionTrigger(ActionTrigger.SELF)
                .timestamp(LocalDateTime.now())
                .channelId(channelId)
                .stateValue(event.isSelfMuted())
                .build();
        eventPublisher.publishEvent(voiceEvent);
    }

    @Override
    public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guildId)) {
            return;
        }

        String username = event.getMember().getUser().getName();
        long userId = event.getMember().getUser().getIdLong();
        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                .guildId(event.getGuild().getIdLong())
                .userId(userId)
                .username(username)
                .eventType(VoiceEventType.SELF_DEAFEN)
                .actionTrigger(ActionTrigger.SELF)
                .timestamp(LocalDateTime.now())
                .channelId(channelId)
                .stateValue(event.isSelfDeafened())
                .build();
        eventPublisher.publishEvent(voiceEvent);
    }

    @Override
    public void onGuildVoiceSuppress(GuildVoiceSuppressEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guildId)) {
            return;
        }

        String username = event.getMember().getUser().getName();
        long userId = event.getMember().getUser().getIdLong();
        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                .guildId(event.getGuild().getIdLong())
                .userId(userId)
                .username(username)
                .eventType(VoiceEventType.SUPPRESS)
                .actionTrigger(ActionTrigger.SELF)
                .timestamp(LocalDateTime.now())
                .channelId(channelId)
                .stateValue(event.isSuppressed())
                .build();
        eventPublisher.publishEvent(voiceEvent);
    }

    @Override
    public void onGuildVoiceStream(GuildVoiceStreamEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guildId)) {
            return;
        }

        String username = event.getMember().getUser().getName();
        long userId = event.getMember().getUser().getIdLong();
        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();

        DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                .guildId(event.getGuild().getIdLong())
                .userId(userId)
                .username(username)
                .eventType(VoiceEventType.STREAM)
                .actionTrigger(ActionTrigger.SELF)
                .timestamp(LocalDateTime.now())
                .channelId(channelId)
                .stateValue(event.isStream())
                .build();
        eventPublisher.publishEvent(voiceEvent);
    }

    @Override
    public void onGuildVoiceVideo(GuildVoiceVideoEvent event) {
        long guildId = event.getGuild().getIdLong();

        if (!monitoredGuilds.isEmpty() && !monitoredGuilds.contains(guildId)) {
            return;
        }

        String username = event.getMember().getUser().getName();
        long userId = event.getMember().getUser().getIdLong();
        Long channelId = event.getVoiceState().getChannel() == null
                ? null
                : event.getVoiceState().getChannel().getIdLong();



        DiscordVoiceStateChangedEvent voiceEvent = DiscordVoiceStateChangedEvent.builder()
                .guildId(event.getGuild().getIdLong())
                .userId(userId)
                .username(username)
                .eventType(VoiceEventType.VIDEO)
                .actionTrigger(ActionTrigger.SELF)
                .timestamp(LocalDateTime.now())
                .channelId(channelId)
                .stateValue(event.isSendingVideo())
                .build();
        eventPublisher.publishEvent(voiceEvent);
    }
}
