package dev.bebomny.beaverdam.discord.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "monitored_containers", schema = "discord")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MonitoredContainer {

    @Id
    @EqualsAndHashCode.Include
    private String containerName;

    private String formatterType;
    private String discordChannelId;

}
