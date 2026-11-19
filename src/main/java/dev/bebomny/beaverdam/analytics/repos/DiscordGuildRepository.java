package dev.bebomny.beaverdam.analytics.repos;

import dev.bebomny.beaverdam.analytics.entities.DiscordGuild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiscordGuildRepository extends JpaRepository<DiscordGuild, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO analytics.discord_guilds (guild_id, name, icon_url, updated_at)
            VALUES (:guildId, :name, :iconUrl, CURRENT_TIMESTAMP)
            ON CONFLICT (guild_id) DO UPDATE
            SET name = EXCLUDED.name,
                icon_url = EXCLUDED.icon_url,
                updated_at = CURRENT_TIMESTAMP
            WHERE analytics.discord_guilds.name IS DISTINCT FROM EXCLUDED.name
               OR analytics.discord_guilds.icon_url IS DISTINCT FROM EXCLUDED.icon_url
            """, nativeQuery = true)
    void upsert(@Param("guildId") Long guildId,
                @Param("name") String name,
                @Param("iconUrl") String iconUrl);
}
