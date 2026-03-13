package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.common.events.WatchpostNewAnimeItemEvent;
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
        buttons.add(Button.primary(ButtonActionType.DOWNLOAD.getId(), "Download"));
        if (!event.isInteresting()) {
            buttons.add(Button.primary(ButtonActionType.SET_AS_INTERESTING.getId(), "Set As Interesting"));
        }

        DiscordUiMessage mapping = DiscordUiMessage.builder()
                .targetModule("watchpost")
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
}
