package dev.bebomny.beaverdam.discord.commands.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import dev.bebomny.beaverdam.discord.commands.ManagementCommand;
import dev.bebomny.beaverdam.downloader.DownloaderCommandApi;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

@Component
public class UploadTorrentSlshCmd extends SlashCommand implements ManagementCommand {

    DownloaderCommandApi downloaderCommandApi;

    public UploadTorrentSlshCmd(DownloaderCommandApi downloaderCommandApi) {
        this.name = "uploadtorrent";
        this.help = "Uploads a torrent torrent to QBittorrent";
        this.ownerCommand = true;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};

        this.downloaderCommandApi = downloaderCommandApi;

        this.options = Collections.singletonList(
                new OptionData(
                        OptionType.STRING,
                        "torrenturl", "The torrent to upload",
                        true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();
        String torrentUrl = Objects.requireNonNull(event.getOption("torrenturl")).getAsString();

        try {
            downloaderCommandApi.downloadTorrentFromUrl(torrentUrl, null, "beaverdam", "2.0");

            event.getHook().sendMessage("Successfully uploaded torrent to QBittorrent").queue();
        } catch (Exception e) {
            event.getHook().sendMessage("Failed to upload torrent. Error: " + e.getMessage()).queue();
        }

    }
}
