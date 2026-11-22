package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import dev.bebomny.beaverdam.discord.repos.DiscordUiMessageRepository;
import dev.bebomny.beaverdam.downloader.events.TorrentDownloadFinishedEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusEventListenerTest {

    @Mock
    private JDA jda;
    @Mock
    private DiscordUiMessageRepository uiMsgRepository;
    @Mock
    private TextChannel textChannel;
    @Mock
    private RestAction<Message> retrieveMessageAction;

    private StatusEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new StatusEventListener(jda, uiMsgRepository);
    }

    @Test
    void onAnimeItemDownloadFinishSkipsGracefullyWhenNoDiscordMappingExists() {
        when(uiMsgRepository.findFirstByTargetModuleAndTargetItemTypeAndTargetItemId("watchpost", "anime_rss_item", 55L))
                .thenReturn(Optional.empty());

        listener.onAnimeItemDownloadFinish(new TorrentDownloadFinishedEvent(55L));

        verify(uiMsgRepository).findFirstByTargetModuleAndTargetItemTypeAndTargetItemId("watchpost", "anime_rss_item", 55L);
        verifyNoInteractions(jda);
    }

    @Test
    void onAnimeItemDownloadFinishRetrievesMessageUsingScopedMapping() {
        DiscordUiMessage mapping = DiscordUiMessage.builder()
                .id(1L)
                .discordChannelId(123456L)
                .discordMessageId(789012L)
                .targetModule("watchpost")
                .targetItemType("anime_rss_item")
                .targetItemId(55L)
                .build();
        when(uiMsgRepository.findFirstByTargetModuleAndTargetItemTypeAndTargetItemId("watchpost", "anime_rss_item", 55L))
                .thenReturn(Optional.of(mapping));
        when(jda.getTextChannelById(123456L)).thenReturn(textChannel);
        when(textChannel.retrieveMessageById(789012L)).thenReturn(retrieveMessageAction);

        listener.onAnimeItemDownloadFinish(new TorrentDownloadFinishedEvent(55L));

        verify(textChannel).retrieveMessageById(789012L);
        verify(retrieveMessageAction).queue(any(), any());
    }
}
