package dev.bebomny.beaverdam.discord.health;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscordHealthIndicator implements HealthIndicator {

    private final JDA jda;

    @Override
    public @Nullable Health health() {
        if (jda == null) {
            return Health.down().withDetail("error", "JDA has not been initialized").build();
        }

        JDA.Status status = jda.getStatus();
        long gatewayPing = jda.getGatewayPing();

        Health.Builder healthBuilder = switch (status) {
            case CONNECTED -> Health.up();

            case ATTEMPTING_TO_RECONNECT, WAITING_TO_RECONNECT,
                 CONNECTING_TO_WEBSOCKET, IDENTIFYING_SESSION -> Health.outOfService();

            default -> Health.down();
        };

        return healthBuilder
                .withDetail("jda_status", status.name())
                .withDetail("gateway_ping_ms", gatewayPing)
                .build();
    }
}
