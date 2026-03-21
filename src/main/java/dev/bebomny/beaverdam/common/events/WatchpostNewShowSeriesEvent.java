package dev.bebomny.beaverdam.common.events;

public record WatchpostNewShowSeriesEvent(
        Long showSeriesId, String showSeriesName,
        Boolean interesting, Boolean ignored, Boolean autoDownload
) {
}
