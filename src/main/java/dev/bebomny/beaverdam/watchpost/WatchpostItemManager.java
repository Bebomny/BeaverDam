package dev.bebomny.beaverdam.watchpost;

import dev.bebomny.beaverdam.watchpost.processors.ContentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchpostItemManager {

    private final List<ContentProcessor<?>> contentProcessors;

    public <T> void routeAndProcess(T rawData, String sourceType, Integer sourceId) {
        boolean processed = false;

        for (ContentProcessor<?> processor : contentProcessors) {
            if (processor.supports(sourceType)) {
                @SuppressWarnings("unchecked")
                ContentProcessor<T> typedProcessor = (ContentProcessor<T>) processor;

                typedProcessor.process(rawData, sourceId);
                processed = true;
            }
        }

        if (!processed) {
            log.atError().log("No processor found for source type: {}", sourceType);
        }
    }
}
