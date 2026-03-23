package dev.bebomny.beaverdam.discord.formatters;

import dev.bebomny.beaverdam.common.events.DockerContainerLogEvent;

import static dev.bebomny.beaverdam.common.helpers.AnsiColorUtils.*;

public class StandardMcServerLogFormatter implements DiscordLogFormatter {

    @Override
    public String getIdentifier() {
        return "STANDARD_MC_FORMATTER";
    }

    @Override
    public String formatLog(DockerContainerLogEvent event) {
        String line = event.logLine();

        if (line.contains("\u001B[m> "))
            return "";
        line = line.replace("\u001B[K", "");
        line = line.replace("\u001B[m", "");

        //Color code INFO, WARN and ERROR lines
        // Schema: \u001b[{format};{color}m
        // INFO -> blue -> \u001b[0;34m
        // WARN -> orange(Yellow) -> \u001b[0;33m
        // ERROR -> red -> \u001b[0;31m
        // Auth -> green -> \u001b[0;32m
        // Autopause -> pink -> \u001b[0;35m

        String lineColor = line.contains("INFO") ? INFO_COLOR
                : line.contains("WARN") ? WARN_COLOR
                : line.contains("ERROR") ? ERROR_COLOR
                //: line.contains("Authenticator") ? AUTH_COLOR
                : line.contains("Autopause") ? AUTOPAUSE_COLOR
                : DEFAULT_COLOR;

        return "```ansi" + '\n' +
                lineColor + line + '\n' +
                "```";
    }
}
