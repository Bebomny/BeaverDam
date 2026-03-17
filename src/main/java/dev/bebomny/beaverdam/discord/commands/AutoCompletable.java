package dev.bebomny.beaverdam.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;

public interface AutoCompletable {
    List<Command.Choice> handleAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event);
    String getCommandName();
}
