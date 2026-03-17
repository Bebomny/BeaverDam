package dev.bebomny.beaverdam.watchpost;

import dev.bebomny.beaverdam.common.dtos.RssFeedSaveResult;

public interface WatchpostCommandApi {
    RssFeedSaveResult addNewRssFeed(String feedName, String feedProcessor, String feedUrl, Long pollInterval, Boolean enabled);
//    ShowSeriesStateDetailsResult updateSeriesState(Long targetItemId, ShowSeriesParam param, Boolean newValue);
}
