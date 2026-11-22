package dev.bebomny.beaverdam.analytics.services;

import dev.bebomny.beaverdam.analytics.api.ActionTrigger;
import dev.bebomny.beaverdam.analytics.api.VoiceEventType;
import dev.bebomny.beaverdam.analytics.api.VoiceStateChangeCommand;
import dev.bebomny.beaverdam.analytics.repos.DiscordGuildRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordUserRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsCommandServiceImplTest {

    @Mock
    private DiscordVoiceStateRepository voiceStateRepository;
    @Mock
    private DiscordVoiceSessionRepository voiceSessionRepository;
    @Mock
    private DiscordGuildRepository discordGuildRepository;
    @Mock
    private DiscordUserRepository discordUserRepository;

    private AnalyticsCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsCommandServiceImpl(
                voiceStateRepository,
                voiceSessionRepository,
                discordGuildRepository,
                discordUserRepository
        );
    }

    @Test
    void recordVoiceStateChangeUpsertsGuildAndUserMetadata() {
        VoiceStateChangeCommand command = VoiceStateChangeCommand.builder()
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

        service.recordVoiceStateChange(command);

        verify(discordGuildRepository).upsert(100L, "BeaverBot_Support_Server", "https://cdn.discordapp.com/icons/100/abc.png");
        verify(discordUserRepository).upsert(1L, "test_user", "https://cdn.discordapp.com/avatars/1/xyz.png");
        verify(voiceStateRepository).save(any());
    }

    @Test
    void upsertGuildMetadataUpsertsGuild() {
        service.upsertGuildMetadata(100L, "New Name", "https://cdn.discordapp.com/icons/100/new.png");

        verify(discordGuildRepository).upsert(100L, "New Name", "https://cdn.discordapp.com/icons/100/new.png");
    }

    @Test
    void upsertUserMetadataUpsertsUser() {
        service.upsertUserMetadata(1L, "test_user_updated", "https://cdn.discordapp.com/avatars/1/new.png");

        verify(discordUserRepository).upsert(1L, "test_user_updated", "https://cdn.discordapp.com/avatars/1/new.png");
    }
}
