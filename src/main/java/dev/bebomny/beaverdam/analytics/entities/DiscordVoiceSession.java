package dev.bebomny.beaverdam.analytics.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discord_voice_sessions", schema = "analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiscordVoiceSession {
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

    @Column(nullable = false, name = "channel_id")
    private Long channelId;

    @Column(nullable = false, name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(nullable = false, name = "left_at")
    private LocalDateTime leftAt;

    @Column(nullable = false, name = "duration_seconds")
    private Long durationSeconds;
}
