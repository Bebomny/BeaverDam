package dev.bebomny.beaverdam.watchpost.repos;

import dev.bebomny.beaverdam.watchpost.entities.AnimeRssItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
            SELECT a FROM AnimeRssItem a
            LEFT JOIN FETCH a.videoDetails
            LEFT JOIN FETCH a.showSeries s
            LEFT JOIN FETCH s.metadata
            LEFT JOIN FETCH a.rssFeed
            WHERE (:interestingOnly = false OR s.isInteresting = true)
            """,
            countQuery = """
                    SELECT count(a) FROM AnimeRssItem a
                    LEFT JOIN a.showSeries s
                    WHERE (:interestingOnly = false OR s.isInteresting = true)
                    """)
    Page<AnimeRssItem> findAnimeItemsWithDetails(@Param("interestingOnly") boolean interestingOnly, Pageable pageable);
}
