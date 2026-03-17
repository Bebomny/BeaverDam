package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.common.dtos.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.common.dtos.ShowSeriesSearchResult;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchpostQueryServiceImpl implements WatchpostQueryApi {

    private final AnimeRssItemRepository animeItemRepository;
    private final ShowSeriesRepository showSeriesRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getShowSeriesIdForAnimeItem(Long animeItemId) {
        return animeItemRepository.findShowSeriesIdByAnimeItemId(animeItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeriesSearchResult> getLastSeenSeries(int limit) {
        return showSeriesRepository.findByOrderByLastSeenDesc(Limit.of(limit))
                .stream()
                .limit(limit)
                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeriesSearchResult> searchSeriesByName(String prefix, int limit) {
        return showSeriesRepository.findBySeriesNameContainingIgnoreCase(prefix, Limit.of(limit))
                .stream()
                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeriesSearchResult> searchSeriesBestMatchByName(String prefix, int limit) {
        return showSeriesRepository.findBestMatchSeries(prefix, Limit.of(limit))
                .stream()
                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<ShowSeriesStateDetailsResult> getSeriesDetailsById(Long id) {
        return showSeriesRepository.findById(id)
                .map(series -> new ShowSeriesStateDetailsResult(
                        series.getId(),
                        series.getSeriesName(),
                        series.getIsInteresting(),
                        series.getIsIgnored(),
                        series.getAutoDownload()
                ));

//        return showSeriesRepository.findBestMatchSeries(name, Limit.of(1))
//                .stream()
//                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
//                .limit(1)
//                .toList()
//                .getFirst();
    }
}
