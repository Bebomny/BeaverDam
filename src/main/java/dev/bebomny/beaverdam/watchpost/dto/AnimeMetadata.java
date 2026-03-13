package dev.bebomny.beaverdam.watchpost.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AnimeMetadata(
        //Extracted by the rss reader
        String infoHash,
        String rawTitle,
        String fileLink,
        String linkType,
        String guid,
        LocalDateTime pubDate,
        String videoCategory,

        //Parsed manually
        String seriesName,
        String groupName,
        String episode,
        String season,
        String rssCategory,

        Long fileSizeBytes,

        Boolean repack,

        //Video Details
        String resolution,
        String source,
        String sourceType,
        String videoType,
        String audioType,
        String subtitles,
        String crc32Checksum
) {
}
