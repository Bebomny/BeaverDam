package dev.bebomny.beaverdam.downloader.listeners;

import dev.bebomny.beaverdam.downloader.events.TorrentDownloadFinishedEvent;
import dev.bebomny.beaverdam.downloader.events.TorrentDownloadRequestEvent;
import dev.bebomny.beaverdam.downloader.services.TorrentManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadEventListenerTest {

    @Mock
    private TorrentManagerService torrentManagerService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DownloadEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new DownloadEventListener(torrentManagerService, eventPublisher);
    }

    @Test
    void onDownloadRequestedPublishesFinishedEventWhenDownloadSucceeds() {
        TorrentDownloadRequestEvent request = new TorrentDownloadRequestEvent(
                42L,
                "https://example.com/ep1.torrent",
                "Lycoris Recoil - 01",
                "anime",
                "beaverdam",
                "2.0"
        );

        listener.onDownloadRequested(request);

        verify(torrentManagerService).processDownload(
                "https://example.com/ep1.torrent",
                "Lycoris Recoil - 01",
                "anime",
                "beaverdam",
                "2.0"
        );
        verify(eventPublisher).publishEvent(new TorrentDownloadFinishedEvent(42L));
    }

    @Test
    void onDownloadRequestedDoesNotPublishFinishedEventWhenTorrentProcessingFails() {
        TorrentDownloadRequestEvent request = new TorrentDownloadRequestEvent(
                42L,
                "https://example.com/ep1.torrent",
                "Lycoris Recoil - 01",
                null,
                "beaverdam",
                "2.0"
        );
        doThrow(new RuntimeException("qBittorrent offline"))
                .when(torrentManagerService)
                .processDownload(anyString(), anyString(), any(), any(), any());

        assertThatThrownBy(() -> listener.onDownloadRequested(request))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(eventPublisher);
    }
}
