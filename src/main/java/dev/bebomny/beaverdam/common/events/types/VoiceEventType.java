package dev.bebomny.beaverdam.common.events.types;

import lombok.Getter;
import net.dv8tion.jda.api.events.guild.voice.*;

@Getter
public enum VoiceEventType {
    CHANNEL_JOIN(GuildVoiceUpdateEvent.class),
    CHANNEL_LEAVE(GuildVoiceUpdateEvent.class),
    CHANNEL_MOVE(GuildVoiceUpdateEvent.class),
    GUILD_MUTE(GuildVoiceGuildMuteEvent.class),
    GUILD_DEAFEN(GuildVoiceGuildDeafenEvent.class),
    SELF_MUTE(GuildVoiceSelfMuteEvent.class),
    SELF_DEAFEN(GuildVoiceSelfDeafenEvent.class),
    SUPPRESS(GuildVoiceSuppressEvent.class),
    STREAM(GuildVoiceStreamEvent.class),
    VIDEO(GuildVoiceVideoEvent.class);

    private final Class<? extends GenericGuildVoiceEvent> jdaEventClass;

    VoiceEventType(Class<? extends GenericGuildVoiceEvent> jdaEventClass) {
        this.jdaEventClass = jdaEventClass;
    }
}
