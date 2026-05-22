package dev.bebomny.beaverdam.common.events;

import lombok.Builder;

@Builder
public record WatchpostNewAnimeItemEvent(
        Long animeItemId,
        String rawItemName,
        String seriesName,
        String episode,
        String sourceFeed,
        String fileLink,

        //Video details
        String resolution,
        String subtitles,
        Long fileSizeBytes,
        String videoCategory,
        String videoSource,
        String sourceType,
        String videoType,
        String audioType,

        //Show series specific
        Long showSeriesId,
        String localizedName,
        String coverImageUrl,

        //Flags
        boolean isInteresting,
        boolean isIgnored,
        boolean autoDownload,
        String customShareRatio,
        String malLink
) implements WatchpostNewItemEvent {

    @Override
    public Long getItemId() {
        return animeItemId;
    }
}
