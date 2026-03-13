package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.common.events.AnimeItemDownloadFinishedEvent;
import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import dev.bebomny.beaverdam.discord.repos.DiscordUiMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusEventListener {

    private final JDA jda;
    private final DiscordUiMessageRepository uiMsgRepository;

    @ApplicationModuleListener
    public void onAnimeItemDownloadFinish(AnimeItemDownloadFinishedEvent event) {
        DiscordUiMessage mapping = uiMsgRepository.findByTargetItemId(event.animeItemId()).getFirst();
        if (mapping == null) {
            log.atWarn().log("Failed to retrieve a uiMapping after a DownloadFinishedEvent for id {}", event.animeItemId());
            return;
        }

        TextChannel channel = jda.getTextChannelById(mapping.getDiscordChannelId());
        if (channel == null) {
            log.atWarn().log("Channel resolution failed for channel id {} from item id {}",
                    mapping.getDiscordChannelId(), event.animeItemId());
            return;
        }

        //Edit the embed with a new "Downloaded At" Field
        channel.retrieveMessageById(mapping.getDiscordMessageId()).queue(message -> {
            if (message.getEmbeds().isEmpty()) return;

            EmbedBuilder newEmbed = new EmbedBuilder(message.getEmbeds().getFirst());
            String timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"));
            newEmbed.addField("Downloaded at", timeNow, true);
            newEmbed.setColor(Color.GREEN);

            message.editMessageEmbeds(newEmbed.build()).queue();
        }, error -> log.atWarn().log("Failed to edit a message for item {}, {}", event.animeItemId(), error.getMessage()));
    }

}
