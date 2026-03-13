package dev.bebomny.beaverdam.watchpost.repos;

import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnimeRssItemRepository extends JpaRepository<AnimeRssItem, Long> {
    boolean existsByInfoHash(String infoHash);

    @EntityGraph(attributePaths = "showSeries")
    Optional<AnimeRssItem> findWithSeriesById(Long id);
}
