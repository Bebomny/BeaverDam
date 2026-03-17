package dev.bebomny.beaverdam.watchpost.repos;

import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnimeRssItemRepository extends JpaRepository<AnimeRssItem, Long> {
    boolean existsByInfoHash(String infoHash);

    @EntityGraph(attributePaths = "showSeries")
    Optional<AnimeRssItem> findWithSeriesById(Long id);

    @Query("SELECT a.showSeries.id FROM AnimeRssItem a WHERE a.id = :animeItemId")
    Optional<Long> findShowSeriesIdByAnimeItemId(@Param("animeItemId") Long animeItemId);
}
