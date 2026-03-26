package dev.bebomny.beaverdam.discord.commands.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.common.dtos.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.common.events.types.ShowSeriesParam;
import dev.bebomny.beaverdam.discord.DiscordActionService;
import dev.bebomny.beaverdam.discord.commands.AutoCompletable;
import dev.bebomny.beaverdam.discord.commands.ManagementCommand;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class UpdateShowSeriesSlshCmd extends SlashCommand implements ManagementCommand, AutoCompletable {

    private final WatchpostQueryApi watchpostQueryApi;
    private final DiscordActionService discordActionService;

    public UpdateShowSeriesSlshCmd(WatchpostQueryApi watchpostQueryApi, DiscordActionService discordActionService) {
        this.name = "updateshowseries";
        this.help = "Updates a selected Show Series";
        this.ownerCommand = true;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};

        this.watchpostQueryApi = watchpostQueryApi;
        this.discordActionService = discordActionService;

        this.options = List.of(
                new OptionData(OptionType.STRING, "showseries", "The Show Series to update", true)
                        .setAutoComplete(true),
                new OptionData(OptionType.STRING, "param", "The parameter to update", true)
                        .addChoice("Ignore", "ignore")
                        .addChoice("Interesting", "interesting")
                        .addChoice("AutoDownload", "autodownload"),
                new OptionData(OptionType.BOOLEAN, "value", "The value to set the parameter to", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();

        Long chosenSeriesId = Objects.requireNonNull(event.getOption("showseries")).getAsLong();
        String chosenParam = Objects.requireNonNull(event.getOption("param")).getAsString();
        Boolean chosenValue = Objects.requireNonNull(event.getOption("value")).getAsBoolean();

        ShowSeriesParam parsedParam = ShowSeriesParam.from(chosenParam);
        if (parsedParam == null) {
            event.getHook().sendMessage("Invalid param %s".formatted(chosenParam)).queue();
            return;
        }

        ShowSeriesStateDetailsResult seriesDetails = discordActionService.updateShowSeries(chosenSeriesId, parsedParam, chosenValue);

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(0x4ef320)
                .setAuthor("The selected Show Series has been updated!")
                .setTitle(seriesDetails.name())
                .setDescription("ID: " + seriesDetails.id())
                .addField("Ignored",
                        parsedParam == ShowSeriesParam.IGNORED
                                ? chosenValue.toString()
                                : seriesDetails.ignored().toString(),
                        true)
                .addField("Interesting",
                        parsedParam == ShowSeriesParam.INTERESTING
                                ? chosenValue.toString()
                                : seriesDetails.interesting().toString(),
                        true)
                .addField("Auto Download",
                        parsedParam == ShowSeriesParam.AUTODOWNLOAD
                                ? chosenValue.toString()
                                : seriesDetails.autoDownload().toString(),
                        true);

        event.getHook().sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public List<Command.Choice> handleAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

        //ShowSeries param
        if (event.getFocusedOption().getName().equalsIgnoreCase("showseries")) {
            String currentValue = event.getFocusedOption().getValue();
            if (currentValue.isBlank()) {
                return watchpostQueryApi.getLastSeenSeries(25)
                        .stream()
                        .map(result ->
                                new Command.Choice(
                                        result.name().substring(0, Math.min(result.name().length(), Command.Choice.MAX_NAME_LENGTH)),
                                        result.id()))
                        .toList();
            }

            return watchpostQueryApi.searchSeriesBestMatchByName(currentValue, 25)
                    .stream()
                    .map(result ->
                            new Command.Choice(
                                    result.name().substring(0, Math.min(result.name().length(), Command.Choice.MAX_NAME_LENGTH)),
                                    result.id()))
                    .toList();
        }

        return List.of();
    }

    @Override
    public String getCommandName() {
        return this.name;
    }
}
