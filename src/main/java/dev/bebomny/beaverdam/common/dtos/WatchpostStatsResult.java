package dev.bebomny.beaverdam.common.dtos;

public record WatchpostStatsResult(
        Long interestingEpisodesPastWeek,
        Long newEpisodesThisSeason,
        Long totalEpisodesToday,
        Long newSeriesThisSeason,
        Long totalEpisodes,
        Long totalInterestingEpisodes,
        Long totalShowSeries,
        Long totalInterestingShowSeries) {
}
