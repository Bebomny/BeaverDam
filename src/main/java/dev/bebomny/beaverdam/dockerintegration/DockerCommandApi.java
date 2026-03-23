package dev.bebomny.beaverdam.dockerintegration;

public interface DockerCommandApi {
    void sendCommandToContainer(String containerName, String command);
    void registerNewContainer(String containerName, String commandStrategyId, String runAsUser, boolean autoAttach);
    void unregisterContainer(String containerName);
}
