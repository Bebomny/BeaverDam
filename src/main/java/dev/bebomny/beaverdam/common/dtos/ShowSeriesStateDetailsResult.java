package dev.bebomny.beaverdam.common.dtos;

public record ShowSeriesStateDetailsResult(
        Long id, String name,
        Boolean interesting, Boolean ignored, Boolean autoDownload) {
}
