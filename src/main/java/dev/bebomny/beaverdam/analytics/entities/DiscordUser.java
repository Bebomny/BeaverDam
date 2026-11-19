package dev.bebomny.beaverdam.analytics.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discord_users", schema = "analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiscordUser {

    @Id
    @Column(name = "user_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long userId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
