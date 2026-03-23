package dev.bebomny.beaverdam.discord.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "monitored_containers", schema = "discord")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoredContainer {

    @Id
    private String containerName;

    private String formatterType;
    private String discordChannelId;

}
