package dev.bebomny.beaverdam.watchpost.events;

public record WatchpostNewShowSeriesEvent(
        Long showSeriesId, String showSeriesName,
        Boolean interesting, Boolean ignored, Boolean autoDownload
) {
}
