package dev.bebomny.beaverdam.common.events;

public record DockerStatusEvent(String containerName, String status) {
}
