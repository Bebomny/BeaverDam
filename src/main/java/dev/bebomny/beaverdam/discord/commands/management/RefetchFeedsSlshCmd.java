package dev.bebomny.beaverdam.discord.commands.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.discord.commands.ManagementCommand;
import dev.bebomny.beaverdam.watchpost.WatchpostCommandApi;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.springframework.stereotype.Component;

@Component
public class RefetchFeedsSlshCmd extends SlashCommand implements ManagementCommand {

    private final WatchpostCommandApi watchpostCommandApi;

    public RefetchFeedsSlshCmd(WatchpostCommandApi watchpostCommandApi) {
        this.name = "refetchfeeds";
        this.help = "Refreshes enabled rss feeds";
        this.ownerCommand = true;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
        this.watchpostCommandApi = watchpostCommandApi;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("Refreshing rss feeds manually. Please wait a couple seconds for the processing to finish")
                .setEphemeral(true)
                .queue();

        watchpostCommandApi.triggerManualFeedRefetch(event.getUser().getName(), "discord");
    }


}
