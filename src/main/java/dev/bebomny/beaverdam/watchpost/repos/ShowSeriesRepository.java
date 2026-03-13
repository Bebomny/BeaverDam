package dev.bebomny.beaverdam.watchpost.repos;

import dev.bebomny.beaverdam.watchpost.entities.ShowSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowSeriesRepository extends JpaRepository<ShowSeries, Long> {
    Optional<ShowSeries> findBySeriesNameIgnoreCase(String seriesName);
}
