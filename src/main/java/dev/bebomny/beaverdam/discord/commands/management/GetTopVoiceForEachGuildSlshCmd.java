package dev.bebomny.beaverdam.discord.commands.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.common.helpers.FormatHelper;
import dev.bebomny.beaverdam.discord.commands.ManagementCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class GetTopVoiceForEachGuildSlshCmd extends SlashCommand implements ManagementCommand {

    @Value("${analytics.discord.monitored_guilds}")
    private Set<Long> monitoredGuilds;

    private final AnalyticsQueryApi analyticsQueryApi;

    public GetTopVoiceForEachGuildSlshCmd(AnalyticsQueryApi analyticsQueryApi) {
        this.name = "gettopvoicetimes";
        this.help = "Gets the top voice for each monitored guild";
        this.ownerCommand = true;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};

        this.analyticsQueryApi = analyticsQueryApi;

        this.options = List.of(new OptionData(OptionType.BOOLEAN, "userawquery", "Whether to use the raw legacy query to fetch the data", true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();

        boolean useRawQuery = Objects.requireNonNull(event.getOption("userawquery")).getAsBoolean();

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.CYAN)
//                .setAuthor("Monitored Guilds Leaderboard")
                .setTitle("Monitored Guilds Leaderboard");

        for (Long guildId : monitoredGuilds) {
            StringBuilder sb = new StringBuilder();
            analyticsQueryApi.getTopVoiceTimeUsers(guildId, 15, useRawQuery)
                    .forEach(user ->
                            sb.append(user.username())
                                    .append(" ")
                                    .append(FormatHelper.formatSecondsToTimeString(user.totalSecondsSpent()))
                                    .append('\n'));
            embed.addField(guildId.toString(), sb.toString(), false);
        }

        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
