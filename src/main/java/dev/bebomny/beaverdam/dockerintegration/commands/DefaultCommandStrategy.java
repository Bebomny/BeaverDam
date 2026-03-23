package dev.bebomny.beaverdam.dockerintegration.commands;

import org.springframework.stereotype.Component;

@Component
public class DefaultCommandStrategy implements ContainerCommandStrategy{
    @Override
    public String getIdentifier() {
        return "DEFAULT_STRATEGY";
    }

    @Override
    public String[] buildCommandArray(String rawCommand) {
        return new String[]{rawCommand};
    }
}
