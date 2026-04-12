package dev.bebomny.beaverdam.watchpost;

import dev.bebomny.beaverdam.common.dtos.RssFeedSaveResult;
import dev.bebomny.beaverdam.common.dtos.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.common.dtos.ShowSeriesUpdateRequest;
import dev.bebomny.beaverdam.common.events.types.ShowSeriesParam;

public interface WatchpostCommandApi {
    RssFeedSaveResult addNewRssFeed(String feedName, String feedProcessor, String feedUrl, Long pollInterval, Boolean enabled);
    ShowSeriesStateDetailsResult updateShowSeriesParam(Long targetItemId, ShowSeriesParam param, Boolean newValue);
    void manuallyAssignAniListIdAndFetchMetadata(Long showSeriesId, Long aniListId);
    boolean removeMetadata(Long showSeriesId);
    void updateShowSeriesState(Long showSeriesId, ShowSeriesUpdateRequest request);
}
