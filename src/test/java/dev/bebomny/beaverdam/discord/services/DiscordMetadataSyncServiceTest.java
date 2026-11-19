package dev.bebomny.beaverdam.discord.services;

import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.common.events.DiscordGuildMetadataUpdatedEvent;
import dev.bebomny.beaverdam.common.events.DiscordUserMetadataUpdatedEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordMetadataSyncServiceTest {

    @Mock
    private JDA jda;
    @Mock
    private AnalyticsQueryApi analyticsQueryApi;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @SuppressWarnings("unchecked")
    void syncMetadataOnStartupPublishesGuildCachedMembersAndBackfillsMissingUsers() {
        Guild guild = mock(Guild.class);
        Member member = mock(Member.class);
        User cachedUser = mock(User.class);
        User backfilledUser = mock(User.class);
        CacheRestAction<User> retrieveAction = mock(CacheRestAction.class);

        when(jda.getGuildById(100L)).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(100L);
        when(guild.getName()).thenReturn("BeaverBot_Support_Server");
        when(guild.getIconUrl()).thenReturn("https://cdn.discordapp.com/icons/100/abc.png");
        when(guild.getMembers()).thenReturn(List.of(member));
        when(member.getUser()).thenReturn(cachedUser);
        when(cachedUser.getIdLong()).thenReturn(1L);
        when(cachedUser.getName()).thenReturn("test_user");
        when(cachedUser.getEffectiveAvatarUrl()).thenReturn("https://cdn.discordapp.com/avatars/1/a.png");

        when(analyticsQueryApi.getUserIdsMissingMetadata()).thenReturn(List.of(1L, 2L));
        when(jda.retrieveUserById(2L)).thenReturn(retrieveAction);
        when(backfilledUser.getIdLong()).thenReturn(2L);
        when(backfilledUser.getName()).thenReturn("offline_test_user");
        when(backfilledUser.getEffectiveAvatarUrl()).thenReturn("https://cdn.discordapp.com/avatars/2/b.png");

        doAnswer(invocation -> {
            Consumer<User> onSuccess = invocation.getArgument(0);
            onSuccess.accept(backfilledUser);
            return null;
        }).when(retrieveAction).queue(any(Consumer.class), any(Consumer.class));

        DiscordMetadataSyncService syncService = new DiscordMetadataSyncService(
                jda, analyticsQueryApi, eventPublisher, Set.of(100L));

        syncService.syncMetadataOnStartup();

        verify(eventPublisher).publishEvent(new DiscordGuildMetadataUpdatedEvent(
                100L, "BeaverBot_Support_Server", "https://cdn.discordapp.com/icons/100/abc.png"));
        verify(eventPublisher).publishEvent(new DiscordUserMetadataUpdatedEvent(
                1L, "test_user", "https://cdn.discordapp.com/avatars/1/a.png"));
        verify(eventPublisher).publishEvent(new DiscordUserMetadataUpdatedEvent(
                2L, "offline_test_user", "https://cdn.discordapp.com/avatars/2/b.png"));
        verify(jda, never()).retrieveUserById(1L);
    }
}
