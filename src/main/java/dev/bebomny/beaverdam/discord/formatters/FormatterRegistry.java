package dev.bebomny.beaverdam.discord.formatters;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FormatterRegistry {

    private final Map<String, DiscordLogFormatter> formatters;
    private final DiscordLogFormatter defaultFormatter = new DefaultLogFormatter();

    public FormatterRegistry(List<DiscordLogFormatter> formatterList) {
        this.formatters = formatterList.stream()
                .collect(Collectors.toMap(DiscordLogFormatter::getIdentifier, formatter -> formatter));
    }

    public DiscordLogFormatter getFormatter(String identifier) {
        return formatters.getOrDefault(identifier, defaultFormatter);
    }
}
