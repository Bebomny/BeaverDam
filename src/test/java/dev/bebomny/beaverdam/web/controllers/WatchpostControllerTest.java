package dev.bebomny.beaverdam.web.controllers;

import dev.bebomny.beaverdam.common.dtos.ShowSeriesUpdateRequest;
import dev.bebomny.beaverdam.watchpost.WatchpostCommandApi;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import dev.bebomny.beaverdam.web.services.SseNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchpostControllerTest {

    @Mock
    private WatchpostQueryApi watchpostQueryApi;
    @Mock
    private WatchpostCommandApi watchpostCommandApi;
    @Mock
    private SseNotificationService sseNotificationService;

    private WatchpostController controller;

    @BeforeEach
    void setUp() {
        controller = new WatchpostController(watchpostQueryApi, watchpostCommandApi, sseNotificationService);
    }

    @Test
    void downloadAnimeItemReturnsOkWhenItemExists() {
        when(watchpostCommandApi.requestAnimeItemDownload(42L)).thenReturn(true);

        ResponseEntity<Void> response = controller.downloadAnimeItem(42L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(watchpostCommandApi).requestAnimeItemDownload(42L);
    }

    @Test
    void downloadAnimeItemReturnsNotFoundWhenItemDoesNotExist() {
        when(watchpostCommandApi.requestAnimeItemDownload(999L)).thenReturn(false);

        ResponseEntity<Void> response = controller.downloadAnimeItem(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateSeriesReturnsBadRequestOnInvariantViolation() {
        ShowSeriesUpdateRequest request = new ShowSeriesUpdateRequest(true, true, false, null);
        doThrow(new IllegalArgumentException("Series cannot be both ignored and interesting"))
                .when(watchpostCommandApi).updateShowSeriesState(10L, request);

        ResponseEntity<Void> response = controller.updateSeries(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateSeriesReturnsNotFoundWhenSeriesMissing() {
        ShowSeriesUpdateRequest request = new ShowSeriesUpdateRequest(true, false, false, null);
        doThrow(new NoSuchElementException("Series id: 10 not found"))
                .when(watchpostCommandApi).updateShowSeriesState(10L, request);

        ResponseEntity<Void> response = controller.updateSeries(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
