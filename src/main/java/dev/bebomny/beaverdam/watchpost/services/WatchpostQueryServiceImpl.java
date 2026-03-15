package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.common.dtos.ShowSeriesSearchResult;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchpostQueryServiceImpl implements WatchpostQueryApi {

    private final ShowSeriesRepository showSeriesRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeriesSearchResult> searchSeriesByName(String prefix, int limit) {
        return showSeriesRepository.findTopBySeriesNameContainingIgnoreCase(prefix)
                .stream()
                .map(series -> new ShowSeriesSearchResult(series.getId(), series.getSeriesName()))
                .limit(limit)
                .toList();
    }
}
