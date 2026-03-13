package dev.bebomny.beaverdam.watchpost.listeners;

import dev.bebomny.beaverdam.common.events.ShowSeriesMarkAsInterestingEvent;
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
    public void onMarkAsInteresting(ShowSeriesMarkAsInterestingEvent event) {
        animeItemRepository.findWithSeriesById(event.targetItemId()).ifPresent(animeItem -> {
            ShowSeries series = animeItem.getShowSeries();
            series.setIsInteresting(true);
            series.setIsIgnored(false);

            showSeriesRepository.save(series);
            log.atInfo().log("Set {} as interesting!", series.getSeriesName());
        });

//        showSeriesRepository.findById(event.showSeriesId()).ifPresent(showSeries -> {
//            showSeries.setIsInteresting(true);
//            showSeries.setIsIgnored(false);
//
//            showSeriesRepository.save(showSeries);
//            log.atInfo().log("Set {} as interesting!", showSeries.getSeriesName());
//        });
    }
}
