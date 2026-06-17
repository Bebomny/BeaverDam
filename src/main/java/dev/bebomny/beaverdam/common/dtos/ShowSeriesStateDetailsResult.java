package dev.bebomny.beaverdam.common.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ShowSeriesStateDetailsResult(
        Long id, String name,
        Boolean interesting, Boolean ignored, Boolean autoDownload, Boolean submitted,
        String customShareRatio,
        LocalDateTime lastSeen, LocalDateTime addedOn) {
}
