package dev.bebomny.beaverdam.discord;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.bebomny.beaverdam.discord.commands.GlobalCommand;
import dev.bebomny.beaverdam.discord.commands.ManagementCommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class JdaConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    @Value("${discord.bot.owner_id}")
    private String ownerId;

    @Value("${discord.bot.main_guild_id}")
    private String mainGuildId;

    @Bean
    public JDA jda(List<SlashCommand> slashCommands, List<ListenerAdapter> listeners) throws InterruptedException {
        List<SlashCommand> globalCommands = slashCommands.stream()
                .filter(cmd -> cmd instanceof GlobalCommand)
                .toList();

        List<SlashCommand> mgmtCommands = slashCommands.stream()
                .filter(cmd -> cmd instanceof ManagementCommand)
                .toList();

        CommandClientBuilder globalCmdBuilder = new CommandClientBuilder();
        globalCmdBuilder.setOwnerId(ownerId);
        globalCmdBuilder.setActivity(null);
        globalCommands.forEach(globalCmdBuilder::addSlashCommand);
//        globalCmdBuilder.addSlashCommands(globalCommands/*new ShowServerIpSlshCmd(), new WhitelistSlshCmd()*/);
        CommandClient globalClient = globalCmdBuilder.build();

        CommandClientBuilder mgmtCmdBuilder = new CommandClientBuilder();
        mgmtCmdBuilder.setOwnerId(ownerId);
        mgmtCmdBuilder.forceGuildOnly(mainGuildId);
        mgmtCmdBuilder.setActivity(null);
        mgmtCommands.forEach(mgmtCmdBuilder::addSlashCommand);
        CommandClient mgmtClient = mgmtCmdBuilder.build();

        JDABuilder builder = JDABuilder.createDefault(botToken)
                .setLargeThreshold(50)
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(
                        MemberCachePolicy.VOICE
                                .or(MemberCachePolicy.ONLINE)
                                .and(MemberCachePolicy.lru(100)
                                        .unloadUnless(MemberCachePolicy.VOICE)))
                .enableIntents(
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.of(Activity.ActivityType.PLAYING, "your mom"))
                .addEventListeners(
                        globalClient,
                        mgmtClient);

        listeners.forEach(builder::addEventListeners);

        return builder.build()
                .awaitReady();
    }
}
