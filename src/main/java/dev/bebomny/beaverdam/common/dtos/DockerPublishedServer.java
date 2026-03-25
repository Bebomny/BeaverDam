package dev.bebomny.beaverdam.common.dtos;

public record DockerPublishedServer(
        String containerName, String address, boolean isOnline, String statusText, String version, String message) {
}
