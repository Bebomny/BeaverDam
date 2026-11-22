package dev.bebomny.beaverdam.dockerintegration.events;

public record DockerContainerLogEvent(String containerName, String logLine, boolean isError) {
}
