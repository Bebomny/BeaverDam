package dev.bebomny.beaverdam.discord.repos;

import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscordUiMessageRepository extends JpaRepository<DiscordUiMessage, Long> {
    DiscordUiMessage findByDiscordMessageId(Long discordMessageId);

    Optional<DiscordUiMessage> findFirstByTargetModuleAndTargetItemTypeAndTargetItemId(
            String targetModule, String targetItemType, Long targetItemId);

    void removeByDiscordMessageId(Long discordMessageId);

    void deleteByDiscordMessageId(Long discordMessageId);
}
