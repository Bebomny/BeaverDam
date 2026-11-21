package dev.bebomny.beaverdam.discord.services;

import dev.bebomny.beaverdam.discord.entities.MonitoredContainer;
import dev.bebomny.beaverdam.discord.repos.MonitoredContainerRepository;
import dev.bebomny.beaverdam.dockerintegration.DockerCommandApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordContainerMonitorServiceTest {

    @Mock
    private MonitoredContainerRepository monitoredContainerRepository;
    @Mock
    private DockerCommandApi dockerCommandApi;

    private DiscordContainerMonitorService service;

    @BeforeEach
    void setUp() {
        service = new DiscordContainerMonitorService(monitoredContainerRepository, dockerCommandApi);
    }

    @Test
    void loadConsoleChannelsPopulatesCacheFromRepository() {
        when(monitoredContainerRepository.findAll()).thenReturn(List.of(
                MonitoredContainer.builder()
                        .containerName("mc-survival")
                        .discordChannelId("111")
                        .formatterType("MC")
                        .build()
        ));

        service.loadConsoleChannels();

        assertThat(service.findContainerNameByChannelId("111")).contains("mc-survival");
        assertThat(service.findContainerNameByChannelId("222")).isEmpty();
    }

    @Test
    void registerMonitoredContainerPersistsRegistersAndUpdatesCacheReassigningOldChannel() {
        service.registerMonitoredContainer("mc-survival", "111", "MC", "MC_CONSOLE", "1000", true);
        assertThat(service.findContainerNameByChannelId("111")).contains("mc-survival");

        service.registerMonitoredContainer("mc-survival", "222", "MC", "MC_CONSOLE", "1000", false);

        ArgumentCaptor<MonitoredContainer> captor = ArgumentCaptor.forClass(MonitoredContainer.class);
        verify(monitoredContainerRepository, times(2)).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getDiscordChannelId()).isEqualTo("222");
        verify(dockerCommandApi).registerNewContainer("mc-survival", "MC_CONSOLE", "1000", true);
        verify(dockerCommandApi).registerNewContainer("mc-survival", "MC_CONSOLE", "1000", false);

        assertThat(service.findContainerNameByChannelId("111")).isEmpty();
        assertThat(service.findContainerNameByChannelId("222")).contains("mc-survival");
    }

    @Test
    void registerMonitoredContainerDoesNotUpdateCacheWhenDockerRegistrationFails() {
        doThrow(new RuntimeException("Docker registration failed"))
                .when(dockerCommandApi)
                .registerNewContainer("mc-broken", "DEFAULT", "1000", true);

        assertThatThrownBy(() -> service.registerMonitoredContainer(
                "mc-broken", "333", "DEFAULT", "DEFAULT", "1000", true))
                .isInstanceOf(RuntimeException.class);

        verify(monitoredContainerRepository).saveAndFlush(any(MonitoredContainer.class));
        assertThat(service.findContainerNameByChannelId("333")).isEmpty();
    }
}
