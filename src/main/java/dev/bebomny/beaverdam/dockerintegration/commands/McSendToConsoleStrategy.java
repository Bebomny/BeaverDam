package dev.bebomny.beaverdam.dockerintegration.commands;

import org.springframework.stereotype.Component;

@Component
public class McSendToConsoleStrategy implements ContainerCommandStrategy {

    @Override
    public String getIdentifier() {
        return "MS_SEND_TO_CONSOLE";
    }

    @Override
    public String[] buildCommandArray(String rawCommand) {
        return new String[]{"mc-send-to-console", rawCommand};
    }
}
