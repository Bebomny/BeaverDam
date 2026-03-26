package dev.bebomny.beaverdam.discord.commands.global;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.analytics.AnalyticsQueryApi;
import dev.bebomny.beaverdam.common.helpers.FormatHelper;
import dev.bebomny.beaverdam.discord.commands.GlobalCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Objects;

@Component
public class VoiceTimeBoardSlshCmd extends SlashCommand implements GlobalCommand {


    private final AnalyticsQueryApi analyticsQueryApi;

    public VoiceTimeBoardSlshCmd(AnalyticsQueryApi analyticsQueryApi) {
        this.name = "voicetimeboard";
        this.help = "Shows the voice chat time board";
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
        this.cooldown = 5;
        this.analyticsQueryApi = analyticsQueryApi;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();
        Long guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
        String guildName = event.getGuild().getName();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle(guildName + "'s Voice Times")
                .setFooter("holi moli");


        StringBuilder sb = new StringBuilder();
        analyticsQueryApi.getTopVoiceTimeUsers(guildId, 15, false)
                .forEach(user ->
                        sb.append(user.username())
                                .append("     ")
                                .append(FormatHelper.formatSecondsToTimeString(user.totalSecondsSpent()))
                                .append('\n'));
        embedBuilder.addField("Top 15", sb.toString(), false);


        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();

    }
}
