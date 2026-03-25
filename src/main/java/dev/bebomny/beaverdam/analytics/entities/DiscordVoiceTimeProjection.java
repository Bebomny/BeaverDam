package dev.bebomny.beaverdam.analytics.entities;

public interface DiscordVoiceTimeProjection {
    String getUsername();
    Long getUserId();
    Long getTotalSeconds();
}
