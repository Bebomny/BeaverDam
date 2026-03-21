package dev.bebomny.beaverdam.discord.repos;

import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscordUiMessageRepository extends JpaRepository<DiscordUiMessage, Long> {
    DiscordUiMessage findByDiscordMessageId(Long discordMessageId);

    List<DiscordUiMessage> findByTargetItemId(Long targetItemId);

    void removeByDiscordMessageId(Long discordMessageId);

    void deleteByDiscordMessageId(Long discordMessageId);
}
