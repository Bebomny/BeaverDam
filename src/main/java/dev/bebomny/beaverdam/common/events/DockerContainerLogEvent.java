package dev.bebomny.beaverdam.common.events;

public record DockerContainerLogEvent(String containerName, String logLine, boolean isError) {
}
