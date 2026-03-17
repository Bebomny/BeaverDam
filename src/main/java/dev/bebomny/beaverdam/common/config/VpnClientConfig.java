package dev.bebomny.beaverdam.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@Slf4j
public class VpnClientConfig {

    @Value("${watchpost.vpn.host}")
    private String vpnHost;

    @Value("${watchpost.vpn.port}")
    private String vpnPort;

    @Value("${watchpost.vpn.enabled}")
    private boolean vpnEnabled;

    @Bean(name = "vpnHttpClient")
    public HttpClient vpnHttpClient() {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15));

        if (vpnEnabled) {
            log.atInfo().log("Configuring Watchpost HttpClient to route through proxy: {}:{}", vpnHost, vpnPort);
            clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(vpnHost, Integer.parseInt(vpnPort))));
        } else {
            log.atWarn().log("VPN routing is DISABLED. Watchpost will use a direct connection.");
        }

        return clientBuilder.build();
    }

}
