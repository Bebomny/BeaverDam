package dev.bebomny.beaverdam.watchpost;

import dev.bebomny.beaverdam.common.dtos.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.common.dtos.ShowSeriesSearchResult;

import java.util.List;
import java.util.Optional;

public interface WatchpostQueryApi {
    Optional<Long> getShowSeriesIdForAnimeItem(Long animeItemId);
    List<ShowSeriesSearchResult> getLastSeenSeries(int limit);
    List<ShowSeriesSearchResult> searchSeriesByName(String prefix, int limit);
    List<ShowSeriesSearchResult> searchSeriesBestMatchByName(String prefix, int limit);
    Optional<ShowSeriesStateDetailsResult> getSeriesDetailsById(Long id);
}
