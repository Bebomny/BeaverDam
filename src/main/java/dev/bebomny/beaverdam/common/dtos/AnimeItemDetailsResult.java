package dev.bebomny.beaverdam.common.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AnimeItemDetailsResult(
        Long animeItemId,
        String rawItemName,
        String seriesName,
        String episode,
        String sourceFeed,
        String fileLink,
        LocalDateTime pubDate,
        LocalDateTime localSaveDate,

        //Video details
        String resolution,
        String subtitles,
        Long fileSizeBytes,
        String videoCategory,
        String videoSource,
        String sourceType,
        String videoType,
        String audioType,

        //Flags
        Long showSeriesId,
        boolean isInteresting,
        boolean isIgnored,
        boolean autoDownload,
        boolean downloaded,
        LocalDateTime downloadedOn,

        //Online details
        Long animeOnlineId,
        String malLink,
        String coverImageUrl,
        String localizedName
) {
}
