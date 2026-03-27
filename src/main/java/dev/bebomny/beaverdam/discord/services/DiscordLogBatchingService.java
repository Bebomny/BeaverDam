package dev.bebomny.beaverdam.discord.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class DiscordLogBatchingService {

    private final DiscordMessagingService messagingService;

    private final Map<String, ConcurrentLinkedQueue<String>> channelQueues = new ConcurrentHashMap<>();

    public void queueLog(String channelId, String formattedLog) {
        channelQueues.computeIfAbsent(channelId, _ -> new ConcurrentLinkedQueue<>()).offer(formattedLog);
    }

    @Scheduled(fixedRate = 3000)
    public void flushLogsToDiscord() {
        channelQueues.forEach((channelId, queue) -> {
            if (queue.isEmpty()) {
                return;
            }

            StringBuilder currentBatch = new StringBuilder();
            String logLine;

            while ((logLine = queue.poll()) != null) {
                // Limit for bots is 2000 chars per message
                if (currentBatch.length() + logLine.length() > 1900) {
                    messagingService.sendTextMessage(channelId, currentBatch.toString());
                    currentBatch.setLength(0);
                }

                currentBatch.append(logLine).append("\n");
            }

            if (!currentBatch.isEmpty()) {
                messagingService.sendTextMessage(channelId, currentBatch.toString());
            }
        });
    }
}
