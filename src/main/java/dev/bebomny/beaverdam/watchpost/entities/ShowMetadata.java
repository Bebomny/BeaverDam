package dev.bebomny.beaverdam.watchpost.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_metadata", schema = "watchpost")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShowMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="show_series_id", unique = true, nullable = false)
    private ShowSeries showSeries;

    private Long malId;
    private Long anilistId;

    private String localizedName;
    private String coverImageUrl;

    @Column(columnDefinition = "TEXT")
    private String synopsis;
    private String genres;
    private String status;

    @Column(name="last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

}
