package dev.bebomny.beaverdam.common.dtos;

public record DownloadDetailsResult(Long animeItemId, String torrentUrl, String seriesName, String episode, String customShareRatio) {
}
