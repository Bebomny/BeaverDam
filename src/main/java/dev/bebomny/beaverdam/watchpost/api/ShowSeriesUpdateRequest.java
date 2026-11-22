package dev.bebomny.beaverdam.watchpost.api;

public record ShowSeriesUpdateRequest(
        Boolean interesting, Boolean ignored, Boolean autoDownload, String customShareRatio
) {
}
