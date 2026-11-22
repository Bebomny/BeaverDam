package dev.bebomny.beaverdam.analytics.services;

import dev.bebomny.beaverdam.analytics.entities.DiscordGuild;
import dev.bebomny.beaverdam.analytics.entities.DiscordUser;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceState;
import dev.bebomny.beaverdam.analytics.entities.DiscordVoiceTimeProjection;
import dev.bebomny.beaverdam.analytics.repos.DiscordGuildRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordUserRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceSessionRepository;
import dev.bebomny.beaverdam.analytics.repos.DiscordVoiceStateRepository;
import dev.bebomny.beaverdam.analytics.api.DiscordGuildVoiceChatDataResult;
import dev.bebomny.beaverdam.analytics.api.DiscordGuildVoiceTimesResult;
import dev.bebomny.beaverdam.analytics.api.DiscordMonitoredGuildResult;
import dev.bebomny.beaverdam.analytics.api.DiscordVoiceChatDataResult;
import dev.bebomny.beaverdam.analytics.api.VoiceEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsQueryApiServiceImplTest {

    @Mock
    private DiscordVoiceStateRepository stateRepository;
    @Mock
    private DiscordVoiceSessionRepository sessionRepository;
    @Mock
    private DiscordGuildRepository guildRepository;
    @Mock
    private DiscordUserRepository userRepository;

    private AnalyticsQueryApiServiceImpl service;

    @BeforeEach
    void setUp() {
        Set<Long> monitoredGuilds = new LinkedHashSet<>(List.of(100L, 200L));
        service = new AnalyticsQueryApiServiceImpl(
                stateRepository,
                sessionRepository,
                guildRepository,
                userRepository,
                monitoredGuilds
        );
    }

    @Test
    void getMonitoredGuildsReturnsPersistedMetadataAndFallsBackWhenMissing() {
        DiscordGuild guild100 = DiscordGuild.builder()
                .guildId(100L)
                .name("BeaverBot_Support_Server")
                .iconUrl("https://cdn.discordapp.com/icons/100/abc.png")
                .updatedAt(LocalDateTime.now())
                .build();

        when(guildRepository.findAllById(any())).thenReturn(List.of(guild100));

        List<DiscordMonitoredGuildResult> results = service.getMonitoredGuilds();

        assertThat(results).containsExactly(
                new DiscordMonitoredGuildResult("100", "BeaverBot_Support_Server", "https://cdn.discordapp.com/icons/100/abc.png"),
                new DiscordMonitoredGuildResult("200", "200", null)
        );
    }

    @Test
    void getGuildVoiceChatDataReturnsEmptyForUnmonitoredGuild() {
        Optional<DiscordGuildVoiceChatDataResult> result = service.getGuildVoiceChatData(999L, "total", 50);

        assertThat(result).isEmpty();
    }

    @Test
    void getGuildVoiceChatDataEnrichesWithPersistedMetadataAndDefaultAvatarFallback() {
        DiscordGuild guild100 = DiscordGuild.builder()
                .guildId(100L)
                .name("BeaverBot_Support_Server")
                .iconUrl("https://cdn.discordapp.com/icons/100/abc.png")
                .updatedAt(LocalDateTime.now())
                .build();

        DiscordVoiceTimeProjection proj1 = projection(1L, "old_test_user", 3600L);
        DiscordVoiceTimeProjection proj2 = projection(2L, "test_user_2", 1800L);

        DiscordUser user1 = DiscordUser.builder()
                .userId(1L)
                .username("new_test_user")
                .avatarUrl("https://cdn.discordapp.com/avatars/1/xyz.png")
                .updatedAt(LocalDateTime.now())
                .build();

        DiscordVoiceState muteState = DiscordVoiceState.builder()
                .guildId(100L)
                .userId(2L)
                .eventType(VoiceEventType.SELF_MUTE)
                .stateValue(true)
                .build();

        when(guildRepository.findById(100L)).thenReturn(Optional.of(guild100));
        when(stateRepository.getVoiceTimesFromRawStates(100L, 100)).thenReturn(List.of(proj1, proj2));
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(user1));
        when(stateRepository.findAllByGuildIdAndStateValueTrue(100L)).thenReturn(List.of(muteState));

        Optional<DiscordGuildVoiceChatDataResult> resultOpt = service.getGuildVoiceChatData(100L, "total", 50);

        assertThat(resultOpt).isPresent();
        DiscordGuildVoiceChatDataResult result = resultOpt.get();
        assertThat(result.guildId()).isEqualTo(100L);
        assertThat(result.guildName()).isEqualTo("BeaverBot_Support_Server");
        assertThat(result.iconUrl()).isEqualTo("https://cdn.discordapp.com/icons/100/abc.png");
        assertThat(result.voiceChatData()).hasSize(2);

        DiscordVoiceChatDataResult alice = result.voiceChatData().get(0);
        assertThat(alice.userId()).isEqualTo(1L);
        assertThat(alice.username()).isEqualTo("new_test_user");
        assertThat(alice.userIconUrl()).isEqualTo("https://cdn.discordapp.com/avatars/1/xyz.png");
        assertThat(alice.totalSecondsSpent()).isEqualTo(3600L);

        DiscordVoiceChatDataResult bob = result.voiceChatData().get(1);
        assertThat(bob.userId()).isEqualTo(2L);
        assertThat(bob.username()).isEqualTo("test_user_2");
        assertThat(bob.userIconUrl()).isEqualTo(AnalyticsQueryApiServiceImpl.DEFAULT_AVATAR_URL);
        assertThat(bob.selfMuteCount()).isEqualTo(1L);
    }

    @Test
    void getVoiceTimesForAllMonitoredGuildsCombinesGuildMetadataAndVoiceTimes() {
        DiscordGuild guild100 = DiscordGuild.builder()
                .guildId(100L)
                .name("BeaverBot_Support_Server")
                .iconUrl("https://cdn.discordapp.com/icons/100/abc.png")
                .updatedAt(LocalDateTime.now())
                .build();
        DiscordVoiceTimeProjection proj = projection(1L, "test_user", 500L);

        when(guildRepository.findAllById(any())).thenReturn(List.of(guild100));
        when(sessionRepository.getVoiceTimesFromSessions(eq(100L), any(PageRequest.class))).thenReturn(List.of(proj));
        when(sessionRepository.getVoiceTimesFromSessions(eq(200L), any(PageRequest.class))).thenReturn(List.of());

        List<DiscordGuildVoiceTimesResult> results = service.getVoiceTimesForAllMonitoredGuilds(20, false);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).guildName()).isEqualTo("BeaverBot_Support_Server");
        assertThat(results.get(0).voiceTimes()).hasSize(1);
        assertThat(results.get(1).guildName()).isEqualTo("200");
        assertThat(results.get(1).voiceTimes()).isEmpty();
    }

    private DiscordVoiceTimeProjection projection(Long userId, String username, Long totalSeconds) {
        DiscordVoiceTimeProjection p = mock(DiscordVoiceTimeProjection.class);
        when(p.getUserId()).thenReturn(userId);
        when(p.getUsername()).thenReturn(username);
        when(p.getTotalSeconds()).thenReturn(totalSeconds);
        return p;
    }
}
