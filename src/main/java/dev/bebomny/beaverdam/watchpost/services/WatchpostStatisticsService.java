package dev.bebomny.beaverdam.watchpost.services;

import dev.bebomny.beaverdam.common.dtos.WatchpostStatsResult;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class WatchpostStatisticsService {

    private final AnimeRssItemRepository animeItemRepository;
    private final ShowSeriesRepository showSeriesRepository;

    @Transactional(readOnly = true)
    public WatchpostStatsResult calculateStatistics() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime startOfWeek = now.minusDays(7).with(LocalTime.MIN);
        LocalDateTime startOfSeason = calculateStartOfSeason(now);

        return new WatchpostStatsResult(
                //Recent stats
                animeItemRepository.countByShowSeriesIsInterestingTrueAndPubDateAfter(startOfWeek),
                animeItemRepository.countByPubDateAfter(startOfSeason),
                animeItemRepository.countByPubDateAfter(startOfDay),
                showSeriesRepository.countByAddedOnAfter(startOfSeason),

                //Total stats
                animeItemRepository.count(),
                animeItemRepository.countByShowSeriesIsInterestingTrue(),
                showSeriesRepository.count(),
                showSeriesRepository.countByIsInterestingTrue()
        );
    }

    private LocalDateTime calculateStartOfSeason(LocalDateTime now) {
        int month = now.getMonthValue();
        int seasonStartMonth = switch (month) {
            case 1, 2, 3 -> 1;
            case 4, 5, 6 -> 4;
            case 7, 8, 9 -> 7;
            default -> 10;
        };

        return LocalDateTime.of(now.getYear(), seasonStartMonth, 1, 0, 0);
    }
}
