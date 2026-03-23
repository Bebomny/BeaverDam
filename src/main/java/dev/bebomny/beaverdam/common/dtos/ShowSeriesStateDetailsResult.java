package dev.bebomny.beaverdam.common.dtos;

import lombok.Builder;

@Builder
public record ShowSeriesStateDetailsResult(
        Long id, String name,
        Boolean interesting, Boolean ignored, Boolean autoDownload) {
}
