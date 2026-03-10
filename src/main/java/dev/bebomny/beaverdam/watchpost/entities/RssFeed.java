package dev.bebomny.beaverdam.watchpost.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rss_feeds", schema = "watchpost")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RssFeed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String rssReaderName;
    private String feedName;
    @Column(length = 1000)
    private String feedUrl;
    private Long pollInterval;
    private Boolean enabled;
    private LocalDateTime lastSuccessfulFetch;
    @Column(length = 1000)
    private String lastErrorMessage;
}
