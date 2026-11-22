package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.downloader.events.TorrentDownloadRequestEvent;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesParam;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesUpdateRequest;
import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.listeners.WatchpostRefetchRequestedEvent;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.RssFeedRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowMetadataRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchpostCommandServiceImplTest {

    @Mock
    private RssFeedRepository rssFeedRepository;
    @Mock
    private ShowSeriesRepository showSeriesRepository;
    @Mock
    private AnimeRssItemRepository animeRssItemRepository;
    @Mock
    private AniListApiService aniListApiService;
    @Mock
    private ShowMetadataRepository showMetadataRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private WatchpostCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WatchpostCommandServiceImpl(
                rssFeedRepository,
                showSeriesRepository,
                animeRssItemRepository,
                aniListApiService,
                showMetadataRepository,
                eventPublisher
        );
    }

    @Test
    void requestAnimeItemDownloadPublishesEventWhenItemExists() {
        ShowSeries series = ShowSeries.builder()
                .id(10L)
                .seriesName("Lycoris Recoil")
                .customShareRatio("2.0")
                .build();
        AnimeRssItem item = AnimeRssItem.builder()
                .id(42L)
                .seriesName("Lycoris Recoil")
                .episode("01")
                .fileLink("https://example.com/ep1.torrent")
                .showSeries(series)
                .build();
        when(animeRssItemRepository.findWithSeriesById(42L)).thenReturn(Optional.of(item));

        boolean result = service.requestAnimeItemDownload(42L);

        assertThat(result).isTrue();
        ArgumentCaptor<TorrentDownloadRequestEvent> captor = ArgumentCaptor.forClass(TorrentDownloadRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new TorrentDownloadRequestEvent(
                42L,
                "https://example.com/ep1.torrent",
                "Lycoris Recoil - 01",
                null,
                "beaverdam",
                "2.0"
        ));
    }

    @Test
    void requestAnimeItemDownloadReturnsFalseAndDoesNotPublishWhenMissing() {
        when(animeRssItemRepository.findWithSeriesById(999L)).thenReturn(Optional.empty());

        boolean result = service.requestAnimeItemDownload(999L);

        assertThat(result).isFalse();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void markAnimeItemDisplayedUpdatesItemWhenPresent() {
        AnimeRssItem item = AnimeRssItem.builder().id(42L).displayed(false).build();
        when(animeRssItemRepository.findById(42L)).thenReturn(Optional.of(item));

        service.markAnimeItemDisplayed(42L);

        assertThat(item.getDisplayed()).isTrue();
        verify(animeRssItemRepository).save(item);
    }

    @Test
    void triggerManualFeedRefetchPublishesInternalRefetchEvent() {
        service.triggerManualFeedRefetch("admin", "discord");

        verify(eventPublisher).publishEvent(new WatchpostRefetchRequestedEvent("admin", "discord"));
    }

    @Test
    void updateShowSeriesParamAppliesDomainInvariants() {
        ShowSeries series = ShowSeries.builder()
                .id(10L)
                .seriesName("Lycoris Recoil")
                .isInteresting(false)
                .isIgnored(true)
                .autoDownload(false)
                .submitted(false)
                .build();
        when(showSeriesRepository.findById(10L)).thenReturn(Optional.of(series));

        ShowSeriesStateDetailsResult result = service.updateShowSeriesParam(10L, ShowSeriesParam.AUTODOWNLOAD, true);

        assertThat(result.autoDownload()).isTrue();
        assertThat(result.interesting()).isTrue();
        assertThat(result.ignored()).isFalse();
    }

    @Test
    void updateShowSeriesStateRejectsContradictoryStateWithoutSaving() {
        ShowSeries series = ShowSeries.builder()
                .id(10L)
                .seriesName("Lycoris Recoil")
                .isInteresting(false)
                .isIgnored(false)
                .autoDownload(false)
                .submitted(false)
                .build();
        when(showSeriesRepository.findById(10L)).thenReturn(Optional.of(series));

        ShowSeriesUpdateRequest invalidRequest = new ShowSeriesUpdateRequest(true, true, false, null);

        assertThatThrownBy(() -> service.updateShowSeriesState(10L, invalidRequest))
                .isInstanceOf(IllegalArgumentException.class);
        verify(showSeriesRepository, never()).save(any());
    }

    @Test
    void updateShowSeriesStateThrowsNoSuchElementWhenSeriesMissing() {
        when(showSeriesRepository.findById(77L)).thenReturn(Optional.empty());

        ShowSeriesUpdateRequest request = new ShowSeriesUpdateRequest(true, false, false, null);

        assertThatThrownBy(() -> service.updateShowSeriesState(77L, request))
                .isInstanceOf(NoSuchElementException.class);
    }
}
