package dev.bebomny.beaverdam.common.events;

public record DiscordGuildMetadataUpdatedEvent(
        Long guildId,
        String guildName,
        String iconUrl
) {
}
