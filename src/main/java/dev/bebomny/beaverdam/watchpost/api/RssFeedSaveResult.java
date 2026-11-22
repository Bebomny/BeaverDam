package dev.bebomny.beaverdam.watchpost.api;

import lombok.Builder;

@Builder
public record RssFeedSaveResult(Boolean success, String feedName, String feedProcessor, String feedUrl, Long pollInterval, Boolean enabled) {
}
