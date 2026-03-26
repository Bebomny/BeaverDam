package dev.bebomny.beaverdam.discord.commands.global;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.common.dtos.DockerPublishedServer;
import dev.bebomny.beaverdam.common.helpers.FormatHelper;
import dev.bebomny.beaverdam.discord.commands.GlobalCommand;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Slf4j
@Component
public class ShowServerIpSlshCmd extends SlashCommand implements GlobalCommand {

    @Value("${discord.docker_servers.fallback_server_address}")
    private String fallbackServerAddress;

    private final DockerCommandApi dockerCommandApi;

    public ShowServerIpSlshCmd(DockerCommandApi dockerCommandApi) {
        this.name = "showserverip";
        this.help = "Shows the currently available server IP address";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM};
        this.cooldown = 5;
        this.dockerCommandApi = dockerCommandApi;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();

        try {
            List<DockerPublishedServer> servers = dockerCommandApi.getPublishedServers();

            if (servers.isEmpty()) {
                event.getHook().sendMessage("No servers are online at this time. :/").queue();
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Minecraft Server IPs")
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
            event.getHook().sendMessage("Failed to retrieve up to date addresses. Try this one for now `" + fallbackServerAddress + "`").queue();
            log.atError().log("Failed to query Docker daemon for server addresses: ", e);
        }
    }
}
