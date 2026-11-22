package dev.bebomny.beaverdam.watchpost;

import dev.bebomny.beaverdam.watchpost.api.RssFeedSaveResult;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesParam;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.watchpost.api.ShowSeriesUpdateRequest;

public interface WatchpostCommandApi {
    RssFeedSaveResult addNewRssFeed(String feedName, String feedProcessor, String feedUrl, Long pollInterval, Boolean enabled);
    ShowSeriesStateDetailsResult updateShowSeriesParam(Long targetItemId, ShowSeriesParam param, Boolean newValue);
    void manuallyAssignAniListIdAndFetchMetadata(Long showSeriesId, Long aniListId);
    boolean removeMetadata(Long showSeriesId);
    void updateShowSeriesState(Long showSeriesId, ShowSeriesUpdateRequest request);
    boolean requestAnimeItemDownload(Long animeItemId);
    void markAnimeItemDisplayed(Long animeItemId);
    void triggerManualFeedRefetch(String requester, String source);
}
