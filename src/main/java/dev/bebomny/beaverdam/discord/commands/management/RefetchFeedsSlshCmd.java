package dev.bebomny.beaverdam.discord.commands.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.common.events.WatchpostRefetchFeedsRequestEvent;
import dev.bebomny.beaverdam.discord.commands.ManagementCommand;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RefetchFeedsSlshCmd extends SlashCommand implements ManagementCommand {

    private final ApplicationEventPublisher eventPublisher;

    public RefetchFeedsSlshCmd(ApplicationEventPublisher applicationEventPublisher) {
        this.name = "refetchfeeds";
        this.help = "Refreshes enabled rss feeds";
        this.ownerCommand = true;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("Refreshing rss feeds manually. Please wait a couple seconds for the processing to finish")
                .setEphemeral(true)
                .queue();

        eventPublisher.publishEvent(new WatchpostRefetchFeedsRequestEvent(event.getUser().getName(), "discord"));
    }


}
