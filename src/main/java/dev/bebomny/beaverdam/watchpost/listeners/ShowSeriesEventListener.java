package dev.bebomny.beaverdam.watchpost.listeners;

import dev.bebomny.beaverdam.common.events.ShowSeriesUpdateParamEvent;
import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import dev.bebomny.beaverdam.watchpost.repos.AnimeRssItemRepository;
import dev.bebomny.beaverdam.watchpost.repos.ShowSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShowSeriesEventListener {

    private final ShowSeriesRepository showSeriesRepository;
    private final AnimeRssItemRepository animeItemRepository;

    @ApplicationModuleListener
    public void onUpdateParam(ShowSeriesUpdateParamEvent event) {
        log.atInfo().log("ShowSeriesUpdateParamEvent: {}", event.toString());

        showSeriesRepository.findById(event.targetItemId()).ifPresent(series -> {
            switch (event.param()) {
                case INTERESTING -> {
                    if (event.newValue()) {
                        series.setIsInteresting(true);
                        series.setIsIgnored(false);
                    } else {
                        series.setIsInteresting(false);
                        series.setAutoDownload(false);
                    }
                }

                case IGNORED ->  {
                    if (event.newValue()) {
                        series.setIsIgnored(true);
                        series.setIsInteresting(false);
                        series.setAutoDownload(false);
                    } else {
                        series.setIsIgnored(false);
                    }
                }

                case AUTODOWNLOAD ->  {
                    if (event.newValue()) {
                        series.setAutoDownload(true);
                        series.setIsInteresting(true);
                        series.setIsIgnored(false);
                    } else {
                        series.setAutoDownload(false);
                    }
                }
            }

            showSeriesRepository.save(series);
            log.atInfo().log("Updated {} param {} to {}!", series.getSeriesName(), event.param().toString(), event.newValue().toString());
        });

//        animeItemRepository.findWithSeriesById(event.targetItemId()).ifPresent(animeItem -> {
//            ShowSeries series = animeItem.getShowSeries();
//
//            switch (event.param()) {
//                case INTERESTING -> {
//                    if (event.newValue()) {
//                        series.setIsInteresting(true);
//                        series.setIsIgnored(false);
//                    } else {
//                        series.setIsInteresting(false);
//                        series.setAutoDownload(false);
//                    }
//                }
//
//                case IGNORED ->  {
//                    if (event.newValue()) {
//                        series.setIsIgnored(true);
//                        series.setIsInteresting(false);
//                        series.setAutoDownload(false);
//                    } else {
//                        series.setIsIgnored(false);
//                    }
//                }
//
//                case AUTODOWNLOAD ->  {
//                    if (event.newValue()) {
//                        series.setAutoDownload(true);
//                        series.setIsInteresting(true);
//                        series.setIsIgnored(false);
//                    } else {
//                        series.setAutoDownload(false);
//                    }
//                }
//            }
//
//            showSeriesRepository.save(series);
//            log.atInfo().log("Updated {} param {} to {}!", series.getSeriesName(), event.param().toString(), event.newValue().toString());
//        });

//        showSeriesRepository.findById(event.showSeriesId()).ifPresent(showSeries -> {
//            showSeries.setIsInteresting(true);
//            showSeries.setIsIgnored(false);
//
//            showSeriesRepository.save(showSeries);
//            log.atInfo().log("Set {} as interesting!", showSeries.getSeriesName());
//        });
    }
}
