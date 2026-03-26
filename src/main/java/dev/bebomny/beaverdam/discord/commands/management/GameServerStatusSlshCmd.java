package dev.bebomny.beaverdam.discord.commands.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.common.dtos.DockerPublishedServer;
import dev.bebomny.beaverdam.common.helpers.FormatHelper;
import dev.bebomny.beaverdam.discord.commands.ManagementCommand;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Slf4j
@Component
public class GameServerStatusSlshCmd extends SlashCommand implements ManagementCommand {

    private final DockerCommandApi dockerCommandApi;

    public GameServerStatusSlshCmd(DockerCommandApi dockerCommandApi) {
        this.name = "gameserverstatus";
        this.help = "Shows information about currently running game servers";
        this.ownerCommand = false;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};

        this.dockerCommandApi = dockerCommandApi;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();

        try {
            List<DockerPublishedServer> servers = dockerCommandApi.getPublishedServers(true);


            if (servers.isEmpty()) {
                event.getHook().sendMessage("No servers found, something may be wrong! Check the docker logs immediately!").queue();
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Game server Statuses")
                    .setColor(Color.GREEN);

            for (DockerPublishedServer server : servers) {
                String statusIcon = server.isOnline() ? ":green_circle:" : ":red_circle:";
                String addressDisplay = server.isOnline() ? "`" + server.address() + "`" : "*Offline*";

                embedBuilder.addField(
                        statusIcon + " " + FormatHelper.formatName(server.containerName()),
                        "**Address:** " + addressDisplay
                                + "\n**Version:** " + server.version()
                                + "\n**Server Status:** " + server.statusText()
                                + "\n" + (server.message() == null ? "" : server.message()),
                        false
                );
            }

            event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
        } catch (Exception e) {
            event.getHook().sendMessage("Failed to query Docker daemon for active game servers!").queue();
            log.atError().log("Failed to query Docker daemon for active game servers! ", e);
        }
    }
}
