package dev.bebomny.beaverdam.analytics.entities;

import dev.bebomny.beaverdam.common.events.types.ActionTrigger;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discord_voice_states", schema = "analytics")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiscordVoiceState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, name = "guild_id")
    private Long guildId;
    @Column(nullable = false, name = "user_id")
    private Long userId;
    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "event_type")
    private VoiceEventType eventType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "action_trigger")
    private ActionTrigger actionTrigger;

    @Column(name = "channel_id")
    private Long channelId;

    //Channel move events
    @Column(name = "previous_channel_id")
    private Long previousChannelId;

    //Mute, Deafen, Stream, Video, Suppress
    @Column(name = "state_value")
    private Boolean stateValue;
}
