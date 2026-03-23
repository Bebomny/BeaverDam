package dev.bebomny.beaverdam.dockerintegration.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.api.model.EventType;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import dev.bebomny.beaverdam.common.events.DockerContainerLogEvent;
import dev.bebomny.beaverdam.common.events.DockerStatusEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerLogMonitorService {

    private final DockerClient dockerClient;
    private final ApplicationEventPublisher eventPublisher;

    private final Set<String> watchedContainers = ConcurrentHashMap.newKeySet();
    private final Map<String, Closeable> activeLogStreams = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeGlobalListener() {
        log.atInfo().log("Starting Global Docker Engine Listener...");

        dockerClient.eventsCmd()
                .withEventTypeFilter(EventType.CONTAINER)
                .exec(new ResultCallback.Adapter<Event>() {
                    @Override
                    public void onNext(Event event) {
                        String containerName = event.getActor().getAttributes().get("name");
                        if (containerName == null || !watchedContainers.contains(containerName)) {
                            return;
                        }

                        if ("start".equals(event.getAction())) {
                            log.atInfo().log("Watched container '{}' started. Attaching logs...", containerName);
                            eventPublisher.publishEvent(new DockerStatusEvent(containerName, "ONLINE"));
                            attachLogStream(containerName);
                        } else if ("die".equals(event.getAction())) {
                            log.atWarn().log("Watched container '{}' died.", containerName);
                            eventPublisher.publishEvent(new DockerStatusEvent(containerName, "OFFLINE"));
                            activeLogStreams.remove(containerName);
                        }
                    }
                });
    }

    public void startMonitoring(String containerName) {
        if (watchedContainers.contains(containerName)) {
            log.atInfo().log("Container '{}' is already being monitored", containerName);
            return;
        }

        watchedContainers.add(containerName);
        attachLogStream(containerName);
    }
    
    private void attachLogStream(String containerName) {
        if (activeLogStreams.containsKey(containerName)) {
            return;
        }

        try {
            Closeable stream = dockerClient.logContainerCmd(containerName)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTail(50)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            boolean isError = frame.getStreamType() == StreamType.STDERR;
                            String logLine = new String(frame.getPayload(), StandardCharsets.UTF_8).trim();

                            if (!logLine.isBlank()) {
                                eventPublisher.publishEvent(new DockerContainerLogEvent(containerName, logLine, isError));
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            log.atError().log("Log stream error for '{}': {}", containerName, throwable.getMessage());
                            activeLogStreams.remove(containerName);
                        }

                        @Override
                        public void onComplete() {
                            log.atInfo().log("Log stream closed for '{}'", containerName);
                            activeLogStreams.remove(containerName);
                        }
                    });
            activeLogStreams.put(containerName, stream);
            log.atInfo().log("Log stream successfully created for '{}'.", containerName);
        } catch (Exception e) {
            log.atError().log("Log stream creation failed for '{}'. Is it running?", containerName, e);
        }
    }
}
