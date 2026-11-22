package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.downloader.events.TorrentDownloadRequestEvent;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesParam;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesUpdateRequest;
import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
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
        when(animeRssItemRepository.existsById(42L)).thenReturn(true);

        boolean result = service.requestAnimeItemDownload(42L);

        assertThat(result).isTrue();
        ArgumentCaptor<AnimeItemDownloadRequestEvent> captor = ArgumentCaptor.forClass(AnimeItemDownloadRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new AnimeItemDownloadRequestEvent(42L, null, "beaverdam"));
    }

    @Test
    void requestAnimeItemDownloadReturnsFalseAndDoesNotPublishWhenMissing() {
        when(animeRssItemRepository.existsById(999L)).thenReturn(false);

        boolean result = service.requestAnimeItemDownload(999L);

        assertThat(result).isFalse();
        verifyNoInteractions(eventPublisher);
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
