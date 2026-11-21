package dev.bebomny.beaverdam.downloader.listeners;

import dev.bebomny.beaverdam.common.dtos.DownloadDetailsResult;
import dev.bebomny.beaverdam.common.events.AnimeItemDownloadFinishedEvent;
import dev.bebomny.beaverdam.common.events.AnimeItemDownloadRequestEvent;
import dev.bebomny.beaverdam.downloader.services.TorrentManagerService;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadEventListenerTest {

    @Mock
    private WatchpostQueryApi watchpostQueryApi;
    @Mock
    private TorrentManagerService torrentManagerService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DownloadEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new DownloadEventListener(watchpostQueryApi, torrentManagerService, eventPublisher);
    }

    @Test
    void onDownloadRequestedPublishesFinishedEventWhenDownloadSucceeds() {
        DownloadDetailsResult details = new DownloadDetailsResult(
                42L,
                "https://example.com/ep1.torrent",
                "Lycoris Recoil",
                "01",
                "2.0"
        );
        when(watchpostQueryApi.getDownloadDetailsForAnimeItemById(42L)).thenReturn(Optional.of(details));

        listener.onDownloadRequested(new AnimeItemDownloadRequestEvent(42L, "anime", "beaverdam"));

        verify(torrentManagerService).processDownload(
                "https://example.com/ep1.torrent",
                "Lycoris Recoil - 01",
                "anime",
                "beaverdam",
                "2.0"
        );
        verify(eventPublisher).publishEvent(new AnimeItemDownloadFinishedEvent(42L));
    }

    @Test
    void onDownloadRequestedDoesNotPublishFinishedEventWhenDetailsMissing() {
        when(watchpostQueryApi.getDownloadDetailsForAnimeItemById(42L)).thenReturn(Optional.empty());

        listener.onDownloadRequested(new AnimeItemDownloadRequestEvent(42L, null, "beaverdam"));

        verifyNoInteractions(torrentManagerService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void onDownloadRequestedDoesNotPublishFinishedEventWhenTorrentProcessingFails() {
        DownloadDetailsResult details = new DownloadDetailsResult(
                42L,
                "https://example.com/ep1.torrent",
                "Lycoris Recoil",
                "01",
                "2.0"
        );
        when(watchpostQueryApi.getDownloadDetailsForAnimeItemById(42L)).thenReturn(Optional.of(details));
        doThrow(new RuntimeException("qBittorrent offline"))
                .when(torrentManagerService)
                .processDownload(anyString(), anyString(), any(), any(), any());

        assertThatThrownBy(() -> listener.onDownloadRequested(new AnimeItemDownloadRequestEvent(42L, null, "beaverdam")))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(eventPublisher);
    }
}
