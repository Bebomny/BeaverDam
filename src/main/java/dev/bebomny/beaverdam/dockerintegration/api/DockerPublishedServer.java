package dev.bebomny.beaverdam.dockerintegration.api;

public record DockerPublishedServer(
        String containerName, String address, boolean isOnline, String statusText, String version, String message) {
}
