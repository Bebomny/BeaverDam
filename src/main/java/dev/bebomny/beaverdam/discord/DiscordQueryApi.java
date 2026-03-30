package dev.bebomny.beaverdam.discord;

import dev.bebomny.beaverdam.common.dtos.DiscordMonitoredGuildResult;

import java.util.List;
import java.util.Map;

public interface DiscordQueryApi {

    List<DiscordMonitoredGuildResult> getMonitoredGuilds();

    DiscordMonitoredGuildResult getMonitoredGuildById(Long guildId);

    Map<Long, String> getIconUrlsByUserIds(List<Long> userIds);
}
