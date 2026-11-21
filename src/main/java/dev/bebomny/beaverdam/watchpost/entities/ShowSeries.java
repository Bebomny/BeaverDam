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

    @OneToOne(mappedBy = "showSeries", cascade = CascadeType.ALL)
    private ShowMetadata metadata;

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

    public void markInteresting(boolean interesting) {
        if (interesting) {
            this.isInteresting = true;
            this.isIgnored = false;
        } else {
            this.isInteresting = false;
            this.autoDownload = false;
        }
    }

    public void markIgnored(boolean ignored) {
        if (ignored) {
            this.isIgnored = true;
            this.isInteresting = false;
            this.autoDownload = false;
        } else {
            this.isIgnored = false;
        }
    }

    public void setAutoDownloadEnabled(boolean autoDownload) {
        if (autoDownload) {
            this.autoDownload = true;
            this.isInteresting = true;
            this.isIgnored = false;
        } else {
            this.autoDownload = false;
        }
    }

    public void markSubmitted() {
        this.submitted = true;
    }

    public void applyStateUpdate(Boolean interesting, Boolean ignored, Boolean autoDownload, String customShareRatio) {
        if (Boolean.TRUE.equals(ignored) && Boolean.TRUE.equals(interesting)) {
            throw new IllegalArgumentException("Series cannot be both ignored and interesting");
        }
        if (Boolean.TRUE.equals(ignored) && Boolean.TRUE.equals(autoDownload)) {
            throw new IllegalArgumentException("Ignored series cannot have autoDownload enabled");
        }
        if (Boolean.TRUE.equals(autoDownload) && Boolean.FALSE.equals(interesting)) {
            throw new IllegalArgumentException("AutoDownload requires series to be marked interesting");
        }

        if (ignored != null) {
            markIgnored(ignored);
        }
        if (interesting != null) {
            markInteresting(interesting);
        }
        if (autoDownload != null) {
            setAutoDownloadEnabled(autoDownload);
        }
        if (customShareRatio != null) {
            this.customShareRatio = customShareRatio;
        }

        markSubmitted();
    }
}
