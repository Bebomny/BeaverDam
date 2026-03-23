package dev.bebomny.beaverdam.dockerintegration.commands;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommandStrategyRegistry {
    private final Map<String, ContainerCommandStrategy> strategies;
    private final DefaultCommandStrategy defaultStrategy =  new DefaultCommandStrategy();

    public CommandStrategyRegistry(List<ContainerCommandStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ContainerCommandStrategy::getIdentifier, strategy -> strategy));
    }

    public ContainerCommandStrategy getStrategy(String identifier) {
        return strategies.getOrDefault(identifier, defaultStrategy);
    }
}
