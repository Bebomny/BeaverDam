package dev.bebomny.beaverdam.watchpost.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video_details", schema = "watchpost")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VideoDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anime_rss_item_id", referencedColumnName = "id", unique = true)
    private AnimeRssItem animeRssItem;

    private String resolution;
    private String source;
    private String sourceType;
    private String videoType;
    private String audioType;
    @Column(length = 500)
    private String subtitles;
    private String crc32Checksum;
}
