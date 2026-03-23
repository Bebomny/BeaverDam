package dev.bebomny.beaverdam.discord.repos;

import dev.bebomny.beaverdam.discord.entities.MonitoredContainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoredContainerRepository extends JpaRepository<MonitoredContainer, String> {
}
