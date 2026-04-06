package dev.bebomny.beaverdam.web.controllers;

import dev.bebomny.beaverdam.common.dtos.AnimeItemDetailsResult;
import dev.bebomny.beaverdam.watchpost.WatchpostCommandApi;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import dev.bebomny.beaverdam.web.security.AdminOnly;
import dev.bebomny.beaverdam.web.services.SseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/watchpost")
@RequiredArgsConstructor
public class WatchpostController {

    private final WatchpostQueryApi watchpostQueryApi;
    private final WatchpostCommandApi watchpostCommandApi;
    private final SseNotificationService sseNotificationService;

    @AdminOnly
    @GetMapping("/latest")
    public ResponseEntity<List<AnimeItemDetailsResult>> getLatestAnime(
            @RequestParam(name = "interesting", defaultValue = "false") boolean interestingOnly,
            Pageable pageable) {
        Page<AnimeItemDetailsResult> page = watchpostQueryApi.getLatestAnimeItems(interestingOnly, pageable);
        return ResponseEntity.ok(page.getContent());
    }

    @AdminOnly
    @PostMapping("/series/{seriesId}/metadata/anilist/{anilistId}")
    public ResponseEntity<Void> manuallyUpdateMetadata(
            @PathVariable Long seriesId,
            @PathVariable Long anilistId) {
        watchpostCommandApi.manuallyAssignAniListIdAndFetchMetadata(seriesId, anilistId);
        return ResponseEntity.ok().build();
    }

    @AdminOnly
    @PostMapping("/series/{seriesId}/metadata/remove")
    public ResponseEntity<Void> removeMetadata(@PathVariable Long seriesId) {
        boolean result = watchpostCommandApi.removeMetadata(seriesId);

        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @AdminOnly
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter streamEvents() {
        return sseNotificationService.createEmitter();
    }

    //TODO: endpoints
    // getShowSeries
    // getLatestShowSeries
    // getUnsubmittedShowSeries
    // getAnimeItems(General)
    // getAnimeItem(by id, by name?, by showseries id?, etc)
    // getAnimeItemsSince(for dynamic updates, to avoid websockets here?)
    // getRssFeeds
    // addRssFeed(create)
    //
}
