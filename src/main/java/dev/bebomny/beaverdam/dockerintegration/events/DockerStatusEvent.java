package dev.bebomny.beaverdam.dockerintegration.events;

public record DockerStatusEvent(String containerName, String status) {
}
