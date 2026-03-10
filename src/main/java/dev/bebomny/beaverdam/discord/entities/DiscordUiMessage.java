package dev.bebomny.beaverdam.discord.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ui_messages", schema = "discord")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordUiMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long discordMessageId;
    private String targetModule; //watchpost or docker
    private Long targetItemId; //The ID of the AnimeRssItem or Docker Container
    private String actionType; //"DOWNLOAD", "IGNORE", "PAUSE_SERVER"
}
