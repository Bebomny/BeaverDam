package dev.bebomny.beaverdam.common.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ShowSeriesDetailsFullResult(
        Long showSeriesId, String showSeriesName,
        Boolean interesting, Boolean ignored, Boolean autoDownload, Boolean submitted,
        String customShareRation,
        LocalDateTime lastSeen, LocalDateTime addedOn,

        ShowMetadataFullResult showMetadata
) {
}
