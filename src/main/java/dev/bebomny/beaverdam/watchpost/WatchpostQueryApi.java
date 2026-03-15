package dev.bebomny.beaverdam.watchpost;

import dev.bebomny.beaverdam.common.dtos.ShowSeriesSearchResult;

import java.util.List;

public interface WatchpostQueryApi {
    List<ShowSeriesSearchResult> searchSeriesByName(String prefix, int limit);
}
