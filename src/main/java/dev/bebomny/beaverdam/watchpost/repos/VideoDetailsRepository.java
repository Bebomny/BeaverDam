package dev.bebomny.beaverdam.watchpost.repos;

import dev.bebomny.beaverdam.watchpost.entities.VideoDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoDetailsRepository extends JpaRepository<VideoDetails, Long> {
}
