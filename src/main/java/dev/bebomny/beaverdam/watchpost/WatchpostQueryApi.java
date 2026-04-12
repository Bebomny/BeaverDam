package dev.bebomny.beaverdam.watchpost;

import dev.bebomny.beaverdam.common.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WatchpostQueryApi {
    Optional<Long> getShowSeriesIdForAnimeItem(Long animeItemId);

    List<ShowSeriesSearchResult> getLastSeenSeries(int limit);

    List<ShowSeriesSearchResult> searchSeriesByName(String prefix, int limit);

    List<ShowSeriesSearchResult> searchSeriesBestMatchByName(String prefix, int limit);

    Optional<ShowSeriesStateDetailsResult> getSeriesDetailsById(Long id);

    Optional<DownloadDetailsResult> getDownloadDetailsForAnimeItemById(Long animeItemId);

    Page<AnimeItemDetailsResult> getLatestAnimeItems(boolean interestingOnly, Pageable pageable);

    Page<ShowSeriesDetailsFullResult> getLatestShowSeries(boolean unsubmittedOnly, Pageable pageable);

    Page<ShowSeriesStateDetailsResult> getShowSeriesWithoutMetadata(Pageable pageable);
}
