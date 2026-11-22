package dev.bebomny.beaverdam.watchpost.api;

import lombok.Builder;

@Builder
public record ShowMetadataFullResult(
        Long metadataId, Long malId, Long anilistId,
        String localizedName, String coverImageUrl,
        String synopsis, String genres, String status
) {
}
