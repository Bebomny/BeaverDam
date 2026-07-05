package dev.bebomny.beaverdam.common.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class VpnHealthIndicator implements HealthIndicator {

    private static final String ENDPOINT_GLUETUN_VPN_PORTFORWARD = "/v1/portforward";
    private static final String ENDPOINT_GLEUTUN_VPN_PUBLICIP = "/v1/publicip/ip";
    private static final String ENDPOINT_GLEUTUN_VPN_STATUS = "/v1/vpn/status";

    private final RestClient vpnRestClient;
    private final RestClient localRestClient;
    private final ObjectMapper objectMapper;

    @Value("${gluetun.url}")
    private String gluetunApiUrl;

    @Value("${gluetun.apikey}")
    private String gluetunApiKey;

    @Value("${watchpost.vpn.enabled}")
    private boolean vpnEnabled;

    private final AtomicReference<Health> cachedHealth = new AtomicReference<>(
            Health.unknown().withDetail("status", "Initializing health check...").build()
    );

    public VpnHealthIndicator(@Qualifier("vpnHttpClient") HttpClient vpnHttpClient, RestClient restClient, ObjectMapper objectMapper) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(vpnHttpClient);
        this.vpnRestClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();

        this.localRestClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Health health() {
        if (!vpnEnabled) {
            return Health.outOfService().withDetail("status", "VPN Disabled by configuration").build();
        }

        return cachedHealth.get();
    }

    @Scheduled(fixedRate = 180000, initialDelay = 10000)
    public void pollVpnStatus() {
        if (!vpnEnabled) {
            return;
        }

        Health.Builder healthBuilder = Health.up();
        boolean overallHealthy = true;

        //Gleutun api general check
        try {
            ResponseEntity<String> statusRsp = localRestClient.get()
                    .uri(gluetunApiUrl + ENDPOINT_GLEUTUN_VPN_STATUS)
                    .header("X-API-Key", gluetunApiKey)
                    .retrieve()
                    .toEntity(String.class);

            if (statusRsp.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(statusRsp.getBody());
                String gluetunStatus = root.path("status").asText("unknown");
                healthBuilder.withDetail("gluetun_status", gluetunStatus);

                if(!"running".equalsIgnoreCase(gluetunStatus)) {
                    overallHealthy = false;
                }
            } else {
                healthBuilder.withDetail("gluetun_api_error", "http " + statusRsp.getStatusCode());
                overallHealthy = false;
            }
        } catch (Exception e) {
            healthBuilder.withDetail("gluetun_api_exception", e.getMessage());
            overallHealthy = false;
        }

        //Gleutun portforward check
        try {
            ResponseEntity<String> portforwardRsp = localRestClient.get()
                    .uri(gluetunApiUrl + ENDPOINT_GLUETUN_VPN_PORTFORWARD)
                    .header("X-API-Key", gluetunApiKey)
                    .retrieve()
                    .toEntity(String.class);

            if (portforwardRsp.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(portforwardRsp.getBody());

                //If successful comes in format {"port":61127,"ports":[61127]}
                int portforwardPort = root.path("port").asInt(-1);
                healthBuilder.withDetail("gluetun_pf_port", portforwardPort > 0 ? portforwardPort : "none");
            }
        } catch (Exception _) {

        }

        //Routing check
        try {
            ResponseEntity<Void> routeRsp = vpnRestClient.head()
                    .uri("1.1.1.1")
                    .retrieve()
                    .toBodilessEntity();

            if (routeRsp.getStatusCode().value() >= 200
                    && routeRsp.getStatusCode().value() < 400) {
                healthBuilder.withDetail("gluetun_routing", "functional");
            } else {
                healthBuilder.withDetail("gluetun_routing", "failed with http " + routeRsp.getStatusCode().value());
                overallHealthy = false;
            }
        } catch (Exception e) {
            healthBuilder.withDetail("gluetun_routing_exception", e.getMessage());
            overallHealthy = false;
        }

        //final update
        if (overallHealthy) {
            cachedHealth.set(healthBuilder.build());
        } else {
            cachedHealth.set(healthBuilder.down().build());
        }
    }
}
