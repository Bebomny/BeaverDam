package dev.bebomny.beaverdam.discord.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ui_messages", schema = "discord")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class DiscordUiMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private Long discordMessageId;
    private Long discordChannelId;
    private String targetModule; //watchpost or docker
    private String targetItemType; //AnimeRssItem or ShowSeries etc
    private Long targetItemId; //The ID of the AnimeRssItem or Docker Container
}
