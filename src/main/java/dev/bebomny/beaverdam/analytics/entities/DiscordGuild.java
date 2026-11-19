package dev.bebomny.beaverdam.analytics.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discord_guilds", schema = "analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiscordGuild {

    @Id
    @Column(name = "guild_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long guildId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
