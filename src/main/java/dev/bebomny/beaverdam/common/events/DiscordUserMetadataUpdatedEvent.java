package dev.bebomny.beaverdam.common.events;

public record DiscordUserMetadataUpdatedEvent(
        Long userId,
        String username,
        String avatarUrl
) {
}
