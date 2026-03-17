package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.discord.commands.AutoCompletable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordAutoCompleteListener extends ListenerAdapter {

    private final List<AutoCompletable> autoCompletableCommands;

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> options = new ArrayList<>();

        String commandUsed = event.getName();
        for (AutoCompletable autoCompletable : autoCompletableCommands) {
            if (!commandUsed.equalsIgnoreCase(autoCompletable.getCommandName())) {
                continue;
            }

            options = autoCompletable.handleAutoCompleteInteraction(event);
        }

        event.replyChoices(options).queue();
    }
}
