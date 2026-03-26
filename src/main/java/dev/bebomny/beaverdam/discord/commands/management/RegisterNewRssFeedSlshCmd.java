package dev.bebomny.beaverdam.discord.commands.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.discord.commands.ManagementCommand;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegisterNewRssFeedSlshCmd extends SlashCommand implements ManagementCommand {

    public RegisterNewRssFeedSlshCmd() {
        this.name = "registerrssfeed";
        this.help = "Register a new RSS Feed to fetch";
        this.ownerCommand = true;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};

        this.options = List.of(
                new OptionData(OptionType.STRING, "feed_name", "Name of the RSS feed", true),
                new OptionData(OptionType.STRING, "feed_processor", "Processor for the RSS feed", true)
                        .addChoices(new Command.Choice("ERAI_RSS", "ERAI_RSS")),
                new OptionData(OptionType.STRING, "url", "URL of the RSS feed", true),
                new OptionData(OptionType.INTEGER, "poll_interval", "How often to poll the rss feed", false),
                new OptionData(OptionType.BOOLEAN, "enabled", "Should it be enabled from the start", false));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("Not implemented yet!").setEphemeral(true).queue();
    }
}
