package dev.bebomny.beaverdam.discord.formatters;

import dev.bebomny.beaverdam.common.events.DockerContainerLogEvent;

public class DefaultLogFormatter implements DiscordLogFormatter {

    @Override
    public String getIdentifier() {
        return "DEFAULT_FORMATTER";
    }

    @Override
    public String formatLog(DockerContainerLogEvent event) {
        return "```ansi" + '\n' +
                event.logLine() + '\n' +
                "```";
    }
}
