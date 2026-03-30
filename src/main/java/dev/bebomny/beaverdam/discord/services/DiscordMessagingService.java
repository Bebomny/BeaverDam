package dev.bebomny.beaverdam.discord.services;

import dev.bebomny.beaverdam.discord.TargetChannel;
import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordMessagingService {

    private final JDA jda;
    private final DiscordActionService discordActionService;

    @Value("${discord.alert.anime.control_panel_channel}")
    private String controlPanelChannelId;

    @Value("${discord.alert.anime.interesting_anime_channel}")
    private String interestingAnimeChannelId;

    @Value("${discord.alert.anime.silent_channel}")
    private String silentAnimeChannelId;

    @Value("${discord.alert.log_channel}")
    private String debugChannelId;


    /**
     * Sends an embed with components(buttons, menus) to the targetChannel.
     * Saves the message id to the DiscordUiMessage database for button interactions
     *
     * @param targetChannel  the channel to send the embed to
     * @param embed          the embed message
     * @param actionRows     actionRows with buttons to attach to the embed. Limit 5 buttons or 1 menu per ActionRow
     * @param partialMapping the partially formed discordUiMessage entity to be saved to the database with the received message id
     */
    public void sendEmbedWithActions(TargetChannel targetChannel, MessageEmbed embed, List<ActionRow> actionRows, DiscordUiMessage partialMapping) {
        if (actionRows.stream().anyMatch(row -> row.getComponents().size() > 5)) {
            log.atError().log("Too many action components in one ActionRow! > 5");
            throw new IllegalArgumentException(String.format("Too many action components! For embed %s", embed.getTitle()));
        }

        TextChannel channel = resolveChannel(targetChannel);
        if (channel == null) return;

        channel.sendMessageEmbeds(embed)
                .addComponents(actionRows)
                .queue(msg -> {
                    partialMapping.setDiscordMessageId(msg.getIdLong());
                    partialMapping.setDiscordChannelId(channel.getIdLong());

                    discordActionService.saveMappingAndPublishAnimeDisplayed(partialMapping);
                }, error -> log.atError().log("Failed to send message to {}", targetChannel, error));
    }

    /**
     * Sends a standard message to the specified targetChannel
     *
     * @param targetChannel the channel to which to send the message
     * @param text          the message to be sent
     */
    public void sendTextMessage(TargetChannel targetChannel, String text) {
        TextChannel channel = resolveChannel(targetChannel);
        if (channel == null) return;

        channel.sendMessage(text).queue();
    }

    /**
     * Sends a standard message to the specified channelId
     *
     * @param channelId the channel id to which to send the message
     * @param text      the message to be sent
     */
    public void sendTextMessage(String channelId, String text) {
        TextChannel channel = resolveChannel(channelId);
        if (channel == null) return;

        channel.sendMessage(text).queue();
    }

    /**
     * Resolves a TextChannel based on the TargetChannel enum
     *
     * @param targetChannel the TargetChannel to resolve
     * @return TextChannel instance or null when the resolution failed
     */
    private TextChannel resolveChannel(TargetChannel targetChannel) {
        String channelId = switch (targetChannel) {
            case CONTROL_PANEL -> controlPanelChannelId;
            case ANIME_INTERESTING_FEED -> interestingAnimeChannelId;
            case ANIME_SILENT_FEED -> silentAnimeChannelId;
            case DEBUG_LOG -> debugChannelId;
        };

        return resolveChannel(channelId);
    }


    /**
     * Resolves a TextChannel based on the channelId
     *
     * @param channelId the id of the channel to resolve
     * @return TextChannel instance or null when the resolution failed
     */
    private TextChannel resolveChannel(String channelId) {
        TextChannel textChannel = jda.getTextChannelById(channelId);
        if (textChannel == null) {
            log.atError().log("Could not resolve text channel with id {}", channelId);
        }
        return textChannel;
    }

    /**
     * Resolves a Guild based on the guildId
     * @param guildId the id of the guild to resolve
     * @return Guild instance or throws `IllegalArgumentException` when the resolution fails
     */
    public Guild resolveGuild(Long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
//            log.atError().log("Could not resolve guild with id {}", guildId);
//            throw new IllegalArgumentException(String.format("Could not resolve guild with id %s", guildId));
        }
        return guild;
    }
}
