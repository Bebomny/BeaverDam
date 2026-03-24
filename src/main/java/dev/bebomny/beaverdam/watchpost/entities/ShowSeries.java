package dev.bebomny.beaverdam.watchpost.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_series", schema = "watchpost")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShowSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String seriesName;
    @Column(unique = true)
    private Long onlineId;
    @Column(length = 1000)
    private String malLink;
    private String releaseSeason;
    private Boolean isInteresting;
    private Boolean isIgnored;
    private Boolean autoDownload;
    private Boolean submitted;
    private String customShareRatio;
    private LocalDateTime lastSeen;
    private LocalDateTime addedOn;
}
