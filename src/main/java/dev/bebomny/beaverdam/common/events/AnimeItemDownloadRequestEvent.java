package dev.bebomny.beaverdam.common.events;

/**
 *
 * @param animeItemId the item id to download
 * @param category the category to assign in qbit
 * @param tags tags for qbit, separated by ','
 */
public record AnimeItemDownloadRequestEvent(Long animeItemId, String category, String tags) {
}
