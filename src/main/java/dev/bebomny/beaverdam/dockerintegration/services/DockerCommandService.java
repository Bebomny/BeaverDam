package dev.bebomny.beaverdam.dockerintegration.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import dev.bebomny.beaverdam.dockerintegration.commands.CommandStrategyRegistry;
import dev.bebomny.beaverdam.dockerintegration.commands.ContainerCommandStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DockerCommandService {

    private final DockerClient dockerClient;
    private final CommandStrategyRegistry strategyRegistry;

    public void executeCommand(String containerName, String strategyId, String rawCommand, String runAsUser) {
        try {
            ContainerCommandStrategy strategy = strategyRegistry.getStrategy(strategyId);
            String[] execCommand = strategy.buildCommandArray(rawCommand);

            String execId = dockerClient.execCreateCmd(containerName)
                    .withCmd(execCommand)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withUser(runAsUser != null ? runAsUser : "1000")
                    .exec()
                    .getId();

            dockerClient.execStartCmd(execId)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            if (frame.getPayload() != null) {
                                log.atDebug().log("Exec output: {}", new String(frame.getPayload()).trim());
                            }
                        }
                    })
                    .awaitCompletion(10, TimeUnit.SECONDS);

            log.atInfo().log("Executed command '{}' in container '{}'", rawCommand, containerName);
        } catch (InterruptedException e) {
            log.atError().log("Command execution timed out in container '{}'", containerName);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Command execution timed out", e);
        } catch (Exception e) {
            log.atError().log("Command execution failed in container '{}'", containerName);
            throw new RuntimeException("Docker execution failed", e);
        }
    }
}
