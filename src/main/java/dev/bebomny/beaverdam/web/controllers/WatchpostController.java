package dev.bebomny.beaverdam.web.controllers;

import dev.bebomny.beaverdam.common.dtos.AnimeItemDetailsResult;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import dev.bebomny.beaverdam.web.security.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchpost")
@RequiredArgsConstructor
public class WatchpostController {

    private final WatchpostQueryApi watchpostQueryApi;

    @AdminOnly
    @GetMapping("/latest")
    public ResponseEntity<List<AnimeItemDetailsResult>> getLatestAnime(
            @RequestParam(name = "interesting", defaultValue = "false") boolean interestingOnly,
            Pageable pageable
    ) {
        Page<AnimeItemDetailsResult> page = watchpostQueryApi.getLatestAnimeItems(interestingOnly, pageable);
        return ResponseEntity.ok(page.getContent());
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
