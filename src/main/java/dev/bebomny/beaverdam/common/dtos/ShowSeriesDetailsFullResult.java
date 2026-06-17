package dev.bebomny.beaverdam.common.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ShowSeriesDetailsFullResult(
        Long showSeriesId, String showSeriesName,
        Boolean interesting, Boolean ignored, Boolean autoDownload, Boolean submitted,
        String customShareRatio,
        LocalDateTime lastSeen, LocalDateTime addedOn,

        ShowMetadataFullResult showMetadata
) {
}
