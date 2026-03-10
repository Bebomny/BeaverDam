package dev.bebomny.beaverdam.watchpost.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_series", schema = "watchpost")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seriesName;
    @Column(unique = true)
    private Long onlineId;
    @Column(length = 1000)
    private String malLink;
    private Boolean isInteresting;
    private Boolean isIgnored;
    private Boolean autoDownload;
    private Boolean submitted;
    private String customShareRatio;
    private LocalDateTime lastSeen;
    private LocalDateTime addedOn;
}
