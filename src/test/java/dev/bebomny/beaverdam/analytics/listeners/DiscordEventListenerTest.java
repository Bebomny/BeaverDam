package dev.bebomny.beaverdam.analytics.listeners;

import dev.bebomny.beaverdam.analytics.repos.DiscordGuildRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordUserRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import dev.bebomny.beaverdam.common.events.DiscordGuildMetadataUpdatedEvent;
import dev.bebomny.beaverdam.common.events.DiscordUserMetadataUpdatedEvent;
import dev.bebomny.beaverdam.common.events.DiscordVoiceStateChangedEvent;
import dev.bebomny.beaverdam.common.events.types.ActionTrigger;
import dev.bebomny.beaverdam.common.events.types.VoiceEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiscordEventListenerTest {

    @Mock
    private DiscordVoiceStateRepository voiceStateRepository;
    @Mock
    private DiscordVoiceSessionRepository voiceSessionRepository;
    @Mock
    private DiscordGuildRepository discordGuildRepository;
    @Mock
    private DiscordUserRepository discordUserRepository;

    private DiscordEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new DiscordEventListener(
                voiceStateRepository,
                voiceSessionRepository,
                discordGuildRepository,
                discordUserRepository
        );
    }

    @Test
    void onVoiceStateChangedUpsertsGuildAndUserMetadata() {
        DiscordVoiceStateChangedEvent event = DiscordVoiceStateChangedEvent.builder()
                .guildId(100L)
                .guildName("BeaverBot_Support_Server")
                .guildIconUrl("https://cdn.discordapp.com/icons/100/abc.png")
                .userId(1L)
                .username("test_user")
                .userAvatarUrl("https://cdn.discordapp.com/avatars/1/xyz.png")
                .timestamp(LocalDateTime.now())
                .eventType(VoiceEventType.CHANNEL_JOIN)
                .actionTrigger(ActionTrigger.SELF)
                .channelId(555L)
                .build();

        listener.onDiscordVoiceStateChangedEvent(event);

        verify(discordGuildRepository).upsert(100L, "BeaverBot_Support_Server", "https://cdn.discordapp.com/icons/100/abc.png");
        verify(discordUserRepository).upsert(1L, "test_user", "https://cdn.discordapp.com/avatars/1/xyz.png");
        verify(voiceStateRepository).save(any());
    }

    @Test
    void onGuildMetadataUpdatedUpsertsGuild() {
        listener.onDiscordGuildMetadataUpdated(
                new DiscordGuildMetadataUpdatedEvent(100L, "New Name", "https://cdn.discordapp.com/icons/100/new.png"));

        verify(discordGuildRepository).upsert(100L, "New Name", "https://cdn.discordapp.com/icons/100/new.png");
    }

    @Test
    void onUserMetadataUpdatedUpsertsUser() {
        listener.onDiscordUserMetadataUpdated(
                new DiscordUserMetadataUpdatedEvent(1L, "test_user_updated", "https://cdn.discordapp.com/avatars/1/new.png"));

        verify(discordUserRepository).upsert(1L, "test_user_updated", "https://cdn.discordapp.com/avatars/1/new.png");
    }
}
