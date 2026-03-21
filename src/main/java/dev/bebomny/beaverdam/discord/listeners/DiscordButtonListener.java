package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.common.dtos.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.common.helpers.ShowSeriesParam;
import dev.bebomny.beaverdam.discord.ButtonActionType;
import dev.bebomny.beaverdam.discord.DiscordActionService;
import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import dev.bebomny.beaverdam.discord.repos.DiscordUiMessageRepository;
import dev.bebomny.beaverdam.watchpost.WatchpostQueryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordButtonListener extends ListenerAdapter {

    private final DiscordUiMessageRepository uiMsgRepository;
    private final DiscordActionService discordActionService;
    private final WatchpostQueryApi watchpostQueryApi;

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        ButtonActionType actionType = ButtonActionType.fromId(event.getComponentId());

        DiscordUiMessage uiMapping = uiMsgRepository.findByDiscordMessageId(event.getMessageIdLong());
        if (uiMapping == null) {
            log.atWarn().log("Got button interaction event on message {}, but couldn't match a UiMapping to it", event.getMessageIdLong());
            event.reply("This button is too old or no longer mapped!").setEphemeral(true).queue();
            return;
        }

        String itemType = uiMapping.getTargetItemType();
        Long itemId = uiMapping.getTargetItemId();

        if (!itemType.equalsIgnoreCase(actionType.getItemType())) {
            log.atWarn().log("Action Type mismatch between the database and action executed! itemType: {}, actionType: {}", itemType, actionType.getItemType());
            return;
        }

        switch (actionType) {
            case ANIME_ITEM_DOWNLOAD -> {
                discordActionService.publishButtonDownload(itemId);

                MessageEmbed editedEmbed = new EmbedBuilder(event.getMessage().getEmbeds().getFirst())
                        .addField("Downloaded started on", LocalDateTime.now().toString(), true)
                        .build();

                event.editMessageEmbeds(editedEmbed).queue();
            }

            case ANIME_ITEM_SET_AS_INTERESTING -> {
                discordActionService.publishButtonSetAsInteresting(itemId);

                event.reply(String.format("Set %s as interesting. Its now going to appear in the interesting series channel.", itemId))
                        .setEphemeral(true)
                        .queue();
            }

            case SHOW_SERIES_INTERESTING -> {
                watchpostQueryApi.getSeriesDetailsById(itemId).ifPresentOrElse(series -> {
                    boolean newValue = !series.interesting();

                    discordActionService.publishShowSeriesUpdateRequest(itemId, ShowSeriesParam.INTERESTING, newValue);

                    // We fake the change to avoid having to wait for the database to respond with a result
                    // as it can take more than 3 seconds which discord requires to answer in.
                    ShowSeriesStateDetailsResult fakeUpdatedDetails = new ShowSeriesStateDetailsResult(
                            series.id(), series.name(),
                            newValue, series.ignored(), series.autoDownload());

                    MessageEmbed newEmbed = WatchpostEventListener.createNewShowSeriesEmbed(fakeUpdatedDetails, false);

                    event.editMessageEmbeds(newEmbed).queue();
                }, () -> event.reply(String.format("Series with id %d doesnt exist, how did you interact with it???",  itemId))
                        .setEphemeral(true)
                        .queue());
            }

            case SHOW_SERIES_IGNORED ->  {
                watchpostQueryApi.getSeriesDetailsById(itemId).ifPresentOrElse(series -> {
                    boolean newValue = !series.ignored();

                    discordActionService.publishShowSeriesUpdateRequest(itemId, ShowSeriesParam.IGNORED, newValue);

                    // We fake the change to avoid having to wait for the database to respond with a result
                    // as it can take more than 3 seconds which discord requires to answer in.
                    ShowSeriesStateDetailsResult fakeUpdatedDetails = new ShowSeriesStateDetailsResult(
                            series.id(), series.name(),
                            series.interesting(), newValue, series.autoDownload());

                    MessageEmbed newEmbed = WatchpostEventListener.createNewShowSeriesEmbed(fakeUpdatedDetails, false);

                    event.editMessageEmbeds(newEmbed).queue();
                }, () -> event.reply(String.format("Series with id %d doesnt exist, how did you interact with it???",  itemId))
                        .setEphemeral(true)
                        .queue());
            }

            case SHOW_SERIES_AUTODOWNLOAD -> {
                watchpostQueryApi.getSeriesDetailsById(itemId).ifPresentOrElse(series -> {
                    boolean newValue = !series.autoDownload();

                    discordActionService.publishShowSeriesUpdateRequest(itemId, ShowSeriesParam.AUTODOWNLOAD, newValue);

                    // We fake the change to avoid having to wait for the database to respond with a result
                    // as it can take more than 3 seconds which discord requires to answer in.
                    ShowSeriesStateDetailsResult fakeUpdatedDetails = new ShowSeriesStateDetailsResult(
                            series.id(), series.name(),
                            series.interesting(), series.ignored(), newValue);

                    MessageEmbed newEmbed = WatchpostEventListener.createNewShowSeriesEmbed(fakeUpdatedDetails, false);

                    event.editMessageEmbeds(newEmbed).queue();
                }, () -> event.reply(String.format("Series with id %d doesnt exist, how did you interact with it???",  itemId))
                        .setEphemeral(true)
                        .queue());
            }

            case SHOW_SERIES_SUBMIT -> {
                watchpostQueryApi.getSeriesDetailsById(itemId).ifPresentOrElse(series -> {

                    discordActionService.publishShowSeriesUpdateRequest(itemId, ShowSeriesParam.SUBMITTED, true);

                    discordActionService.removeUiMessageMappingByMessageId(event.getMessageIdLong());

                    List<ActionRow> updatedComponents = event.getMessage().getComponents().stream()
                            .map(actionRow -> ActionRow.of(
                                    actionRow.asActionRow().getButtons().stream()
                                            .map(Button::asDisabled)
                                            .collect(Collectors.toList())))
                            .toList();

                    MessageEmbed newEmbed = WatchpostEventListener.createNewShowSeriesEmbed(series, true);

                    event.editMessageEmbeds(newEmbed)
                            .setComponents(updatedComponents)
                            .queue();
                }, () -> event.reply(String.format("Series with id %d doesnt exist, how did you interact with it???",  itemId))
                        .setEphemeral(true)
                        .queue());
            }
        }
    }
}
