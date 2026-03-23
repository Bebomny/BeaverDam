package dev.bebomny.beaverdam.dockerintegration.commands;

public interface ContainerCommandStrategy {
    String getIdentifier();
    String[] buildCommandArray(String rawCommand);
}
