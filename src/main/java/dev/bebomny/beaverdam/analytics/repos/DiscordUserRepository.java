package dev.bebomny.beaverdam.analytics.repos;

import dev.bebomny.beaverdam.analytics.entities.DiscordUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscordUserRepository extends JpaRepository<DiscordUser, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO analytics.discord_users (user_id, username, avatar_url, updated_at)
            VALUES (:userId, :username, :avatarUrl, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id) DO UPDATE
            SET username = EXCLUDED.username,
                avatar_url = EXCLUDED.avatar_url,
                updated_at = CURRENT_TIMESTAMP
            WHERE analytics.discord_users.username IS DISTINCT FROM EXCLUDED.username
               OR analytics.discord_users.avatar_url IS DISTINCT FROM EXCLUDED.avatar_url
            """, nativeQuery = true)
    void upsert(@Param("userId") Long userId,
                @Param("username") String username,
                @Param("avatarUrl") String avatarUrl);

    @Query(value = """
            SELECT DISTINCT vs.user_id
            FROM analytics.discord_voice_states vs
            LEFT JOIN analytics.discord_users u ON u.user_id = vs.user_id
            WHERE u.user_id IS NULL
            """, nativeQuery = true)
    List<Long> findUserIdsMissingMetadata();
}
