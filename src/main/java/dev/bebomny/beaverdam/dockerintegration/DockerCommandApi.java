package dev.bebomny.beaverdam.dockerintegration;

import dev.bebomny.beaverdam.common.dtos.DockerPublishedServer;

import java.util.List;

public interface DockerCommandApi {
    void sendCommandToContainer(String containerName, String command);
    void registerNewContainer(String containerName, String commandStrategyId, String runAsUser, boolean autoAttach);
    void unregisterContainer(String containerName);
    List<DockerPublishedServer> getPublishedServers(boolean showAll);
}
