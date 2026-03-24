package dev.bebomny.beaverdam.watchpost.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rss_feeds", schema = "watchpost")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RssFeed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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
