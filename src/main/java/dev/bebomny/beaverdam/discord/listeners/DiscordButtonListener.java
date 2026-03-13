package dev.bebomny.beaverdam.discord.listeners;

import dev.bebomny.beaverdam.discord.ButtonActionType;
import dev.bebomny.beaverdam.discord.DiscordActionService;
import dev.bebomny.beaverdam.discord.entities.DiscordUiMessage;
import dev.bebomny.beaverdam.discord.repos.DiscordUiMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordButtonListener extends ListenerAdapter {

    private final DiscordUiMessageRepository uiMsgRepository;
    private final DiscordActionService discordActionService;

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        ButtonActionType actionType = ButtonActionType.fromId(event.getComponentId());

        DiscordUiMessage uiMapping = uiMsgRepository.findByDiscordMessageId(event.getMessageIdLong());
        if (uiMapping == null) {
            log.atWarn().log("Got button interaction event on message {}, but couldn't match a UiMapping to it", event.getMessageIdLong());
            event.reply("This button is too old or no longer mapped!").setEphemeral(true).queue();
            return;
        }

        Long itemId = uiMapping.getTargetItemId();

        switch (actionType) {
            case DOWNLOAD -> {
                discordActionService.publishButtonDownload(itemId);

                MessageEmbed editedEmbed = new EmbedBuilder(event.getMessage().getEmbeds().getFirst())
                        .addField("Downloaded started on", LocalDateTime.now().toString(), true)
                        .build();

                event.editMessageEmbeds(editedEmbed).queue();
            }

            case SET_AS_INTERESTING -> {
                discordActionService.publishButtonSetAsInteresting(itemId);

                event.reply(String.format("Set %s as interesting. Its now going to appear in the interesting series channel.", itemId))
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
