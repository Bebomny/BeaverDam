package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.common.dtos.ShowSeriesStateDetailsResult;
import dev.bebomny.beaverdam.common.events.WatchpostNewAnimeItemEvent;
import dev.bebomny.beaverdam.common.events.WatchpostNewShowSeriesEvent;
import dev.bebomny.beaverdam.common.helpers.AnimeHelper;
import dev.bebomny.beaverdam.common.helpers.FileFormatter;
import dev.bebomny.beaverdam.discord.ButtonActionType;
import dev.bebomny.beaverdam.discord.DiscordMessagingService;
import dev.bebomny.beaverdam.discord.TargetChannel;
import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WatchpostEventListener {

    private final DiscordMessagingService messagingService;

    @ApplicationModuleListener
    public void onNewAnimeItem(WatchpostNewAnimeItemEvent event) {
        if (event.seriesName() == null || event.rawItemName() == null) {
            log.atError().log("Malformed Event. New anime item has missing crucial fields");
            return;
        }

        TargetChannel target = event.isInteresting() ? TargetChannel.ANIME_INTERESTING_FEED : TargetChannel.ANIME_SILENT_FEED;

        MessageEmbed embed = createNewAnimeItemEmbed(event);

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.link(AnimeHelper.encodeURL(event.fileLink()), "Torrent File"));
        buttons.add(ButtonActionType.ANIME_ITEM_DOWNLOAD.createButton());
        if (!event.isInteresting()) {
            buttons.add(ButtonActionType.ANIME_ITEM_SET_AS_INTERESTING.createButton());
        }

        DiscordUiMessage mapping = DiscordUiMessage.builder()
                .targetModule("watchpost")
                .targetItemType("anime_rss_item")
                .targetItemId(event.animeItemId())
                .build();

        messagingService.sendEmbedWithActions(target, embed, List.of(ActionRow.of(buttons)), mapping);
    }

    public static MessageEmbed createNewAnimeItemEmbed(WatchpostNewAnimeItemEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(0x2adbfe) //Cyanish - 0x2adbfe
                .setAuthor("New Anime Episode Found!")
                .setTitle(event.seriesName())
                .setFooter(event.rawItemName());

        if (event.episode() != null) {
            embedBuilder.setDescription("Episode %s %s".formatted(event.episode(), event.videoType()));
        }

        if (event.sourceFeed() != null) {
            embedBuilder.addField("From", event.sourceFeed(), true);
        }

        if (event.resolution() != null) {
            embedBuilder.addField("Resolution", event.resolution(), true);
        }

        if (event.videoSource() != null
                && event.sourceType() != null
                && event.videoType() != null
                && event.audioType() != null) {
            embedBuilder.addField("InfoMKV", (event.videoType() + " " + event.audioType() + " " + event.videoSource() + " " + event.sourceType()), true);
        }

        if (event.subtitles() != null) {
            embedBuilder.addField("Subtitles",
                    event.subtitles()
                            .replace("][", ",")
                            .replace("[", "")
                            .replace("]", ""), true);
        }

        if (event.fileSizeBytes() != null) {
            embedBuilder.addField("File Size", FileFormatter.parseBytesToFileSizeString(event.fileSizeBytes()), true);
        }

        if (event.videoCategory() != null) {
            embedBuilder.addField("Category", event.videoCategory(), true);
        }

        if (event.autoDownload()) {
            embedBuilder.addField("Auto Download Start", Date.from(Instant.now()).toString(), true);
        }

        return embedBuilder.build();
    }

    @ApplicationModuleListener
    public void onNewShowSeriesEvent(WatchpostNewShowSeriesEvent event) {
        MessageEmbed embed = createNewShowSeriesEmbed(
                event.showSeriesName(), event.showSeriesId(),
                event.interesting(), event.ignored(), event.autoDownload(), false);

        List<Button> buttons = new ArrayList<>();
        buttons.add(ButtonActionType.SHOW_SERIES_SUBMIT.createButton());
        buttons.add(ButtonActionType.SHOW_SERIES_INTERESTING.createButton());
        buttons.add(ButtonActionType.SHOW_SERIES_IGNORED.createButton());
        buttons.add(ButtonActionType.SHOW_SERIES_AUTODOWNLOAD.createButton());

        DiscordUiMessage mapping = DiscordUiMessage.builder()
                .targetModule("watchpost")
                .targetItemType("show_series")
                .targetItemId(event.showSeriesId())
                .build();

        messagingService.sendEmbedWithActions(
                TargetChannel.CONTROL_PANEL,
                embed, List.of(ActionRow.of(buttons)),
                mapping);
    }

    public static MessageEmbed createNewShowSeriesEmbed(ShowSeriesStateDetailsResult seriesDetails, Boolean submitted) {
        return createNewShowSeriesEmbed(
                seriesDetails.name(), seriesDetails.id(),
                seriesDetails.interesting(), seriesDetails.ignored(), seriesDetails.autoDownload(),
                submitted);
    }

    public static MessageEmbed createNewShowSeriesEmbed(String name, Long id,
                                                        Boolean isInteresting, Boolean isIgnored, Boolean autoDownload,
                                                        Boolean submitted) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(0x4ef320)
                .setAuthor("New Unknown ShowSeries Found!")
                .setTitle(name)
                .setDescription("ID: " + id)
                .addField("Ignored", isIgnored.toString(), true)
                .addField("Interesting", isInteresting.toString(), true)
                .addField("Auto Download", autoDownload.toString(), true);

        if (submitted) {
            eb.setFooter("Submitted at %s".formatted(Date.from(Instant.now())));
        }

        return eb.build();
    }
}
