package dev.bebomny.beaverdam.watchpost.repos;

import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowSeriesRepository extends JpaRepository<ShowSeries, Long> {
    Optional<ShowSeries> findBySeriesNameIgnoreCase(String seriesName);

    List<ShowSeries> findBySeriesNameContainingIgnoreCase(String seriesName, Limit limit);

    List<ShowSeries> findByOrderByLastSeenDesc(Limit limit);

    @Query("SELECT s FROM ShowSeries s " +
            "WHERE LOWER(s.seriesName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY LOCATE(LOWER(:search), LOWER(s.seriesName)) ASC, LENGTH(s.seriesName) ASC")
    List<ShowSeries> findBestMatchSeries(@Param("search") String search, Limit limit);

    @Query("SELECT s FROM ShowSeries s WHERE NOT EXISTS (SELECT 1 FROM ShowMetadata m WHERE m.showSeries = s)")
    List<ShowSeries> findSeriesWithoutMetadata();

    @EntityGraph(attributePaths = "metadata")
    Page<ShowSeries> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "metadata")
    Page<ShowSeries> findAllBySubmittedFalse(Pageable pageable);

    Page<ShowSeries> findAllByMetadataNull(Pageable pageable);

    Long countByAddedOnAfter(LocalDateTime date);

    Long countByIsInterestingTrue();
}
