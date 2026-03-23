package dev.bebomny.beaverdam.discord.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.discord.entities.MonitoredContainer;
import dev.bebomny.beaverdam.discord.repos.MonitoredContainerRepository;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class MonitorContainerSlshCmd extends SlashCommand implements ManagementCommand{

    private final MonitoredContainerRepository monitoredContainerRepository;
    private final DockerCommandApi dockerCommandApi;

    public MonitorContainerSlshCmd(MonitoredContainerRepository containerRepo, DockerCommandApi dockerCommandApi) {
        this.name = "monitorcontainer";
        this.help = "Hooks the server output and prints it to the chat";
        this.ownerCommand = true;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};

        this.monitoredContainerRepository = containerRepo;
        this.dockerCommandApi = dockerCommandApi;

        this.options = List.of(
                new OptionData(OptionType.STRING, "container", "The name of the container to monitor", true)
                        .setAutoComplete(true),
                new OptionData(OptionType.CHANNEL, "channel", "The channel to send the logs to", true),
                new OptionData(OptionType.STRING, "formatter", "The formatter to use for discord", false),
                new OptionData(OptionType.STRING, "strategy", "The input strategy for bi-directional communication", false),
                new OptionData(OptionType.STRING, "run_as", "User ID to run the commands with", false),
                new OptionData(OptionType.BOOLEAN, "auto_attach", "Whether to start monitoring on startup", false)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();

        try {
            String containerName = Objects.requireNonNull(event.getOption("container")).getAsString();
            String channelId = Objects.requireNonNull(event.getOption("channel")).getAsString();
            String formatter = event.getOption("formatter", "DEFAULT_FORMATTER", OptionMapping::getAsString);
            String strategy = event.getOption("strategy", "DEFAULT_STRATEGY", OptionMapping::getAsString);
            String runAs = event.getOption("run_as", "1000", OptionMapping::getAsString);
            boolean autoAttach = event.getOption("auto_attach", false, OptionMapping::getAsBoolean);

            MonitoredContainer mapping = MonitoredContainer.builder()
                    .containerName(containerName)
                    .discordChannelId(channelId)
                    .formatterType(formatter)
                    .build();

            monitoredContainerRepository.save(mapping);
            log.atInfo().log("Saved Discord MonitoredContainer mapping for container: {}", containerName);

            dockerCommandApi.registerNewContainer(containerName, strategy, runAs, autoAttach);

            event.getHook().sendMessage(
                    String.format("Successfully linked container '%s'.\nLogs will be sent to <#%s> using formatter '%s'.",
                            containerName, channelId, formatter))
                    .queue();
        } catch (Exception e) {
            log.atError().log("Failed to execute /monitorcontainer command", e);
            event.getHook().sendMessage("Failed to setup monitor: " + e.getMessage()).queue();
        }
    }
}
