package dev.bebomny.beaverdam.watchpost.repos;

import dev.bebomny.beaverdam.watchpost.entities.ShowMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowMetadataRepository extends JpaRepository<ShowMetadata, Long> {
    Optional<ShowMetadata> findByShowSeriesId(Long showSeriesId);
}
