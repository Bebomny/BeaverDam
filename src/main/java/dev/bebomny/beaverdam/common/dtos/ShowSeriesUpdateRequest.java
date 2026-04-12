package dev.bebomny.beaverdam.common.dtos;

public record ShowSeriesUpdateRequest(
        Boolean interesting, Boolean ignored, Boolean autoDownload, String customShareRatio
) {
}
