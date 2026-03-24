package dev.bebomny.beaverdam.watchpost.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anime_rss_items", schema = "watchpost")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnimeRssItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rss_feed_id")
    private RssFeed rssFeed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_series_id")
    private ShowSeries showSeries;

    private Long internalFileId;

    @Column(length = 1000)
    private String rawItemName;
    @Column(length = 1000)
    private String seriesName;
    @Column(length = 1000)
    private String localizedName;
    private String groupName;
    private String episode;
    private String season;

    private String rssCategory;
    private String videoCategory; // prev: erai-category: [Airing] etc
    @Column(length = 1200)
    private String fileLink;
    private Long fileSize;
    private String guid;
    private String onlineId;
    private LocalDateTime pubDate;
    private LocalDateTime localSaveDate;

    @Column(unique = true, nullable = false)
    private String infoHash;

    private Boolean downloaded;
    private LocalDateTime downloadedOn;
    private Boolean displayed;
    private Boolean repack;
}
