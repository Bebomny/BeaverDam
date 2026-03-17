package dev.bebomny.beaverdam.watchpost.repos;

import dev.bebomny.beaverdam.watchpost.entities.RssFeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RssFeedRepository extends JpaRepository<RssFeed, Integer> {
    List<RssFeed> findByEnabledTrue();

    boolean existsByFeedUrl(String feedUrl);
}
