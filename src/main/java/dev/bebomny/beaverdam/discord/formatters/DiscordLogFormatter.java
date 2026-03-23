package dev.bebomny.beaverdam.discord.formatters;

import dev.bebomny.beaverdam.common.events.DockerContainerLogEvent;

public interface DiscordLogFormatter {
    String getIdentifier();
    String formatLog(DockerContainerLogEvent event);
}
