package dev.bebomny.beaverdam.discord.commands.global;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.common.dtos.DockerPublishedServer;
import dev.bebomny.beaverdam.discord.commands.GlobalCommand;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class WhitelistSlshCmd extends SlashCommand implements GlobalCommand {

    private final DockerCommandApi dockerCommandApi;

    public WhitelistSlshCmd(DockerCommandApi dockerCommandApi) {
        this.name = "whitelist";
        this.help = "Manage whitelists on minecraft servers";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL};
        this.cooldown = 5;

        this.dockerCommandApi = dockerCommandApi;

        List<Command.Choice> serverChoices = dockerCommandApi.getPublishedServers()
                .stream()
                .map(DockerPublishedServer::containerName)
                .map(s -> new Command.Choice(s, s))
                .toList();

        this.options = List.of(
                new OptionData(OptionType.STRING, "username", "Username of the player you want to add", true),
                new OptionData(OptionType.STRING, "server", "Which minecraft server to manage | Default: Trails and Tails", false)
                        .addChoices(serverChoices));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();

        String playerName = Objects.requireNonNull(event.getOption("username")).getAsString();
        String containerName = event.getOption("server", "mc-trails-server", OptionMapping::getAsString);

        try {

            String rawCommand = "whitelist add " + playerName;

            dockerCommandApi.sendCommandToContainer(containerName, rawCommand);

            event.getHook().sendMessage("User '" + playerName + "' added to the whitelist on '" + containerName + "'").queue();
        } catch (IllegalArgumentException e) {
            event.getHook().sendMessage("Something went wrong, please try again.").queue();
            log.atError().log("Failed to send whitelist command to '{}', is the container monitored?", containerName, e);
        } catch (Exception e) {
            event.getHook().sendMessage("Something went wrong, please try again.").queue();
            log.atError().log("Failed to send whitelist command to '{}'", containerName, e);
        }
    }
}
