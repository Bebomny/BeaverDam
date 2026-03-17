package dev.bebomny.beaverdam.common.dtos;

import lombok.Builder;

@Builder
public record RssFeedSaveResult(Boolean success, String feedName, String feedProcessor, String feedUrl, Long pollInterval, Boolean enabled) {
}
