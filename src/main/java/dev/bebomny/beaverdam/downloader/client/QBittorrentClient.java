package dev.bebomny.beaverdam.downloader.client;

import dev.bebomny.beaverdam.downloader.config.QBittorrentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class QBittorrentClient {

    private final RestClient restClient;
    private final QBittorrentProperties properties;

    private final AtomicReference<String> authCookie = new AtomicReference<>(null);

    /**
     * Adds a torrent to qbittorrent
     *
     * @param torrentBytes the torrent file in Byte Array form
     * @param torrentName the name of the torrent file
     * @param category categories to assign to the torrent in qbit
     * @param tags tags to assign to the torrent split by ','
     * @param ratioLimit the share ratio limit to set on the torrent. Leave blank for no share limit
     */
    public void addTorrent(byte[] torrentBytes, String torrentName, String category, String tags, String ratioLimit) {
        if (authCookie.get() == null) {
            authenticate();
        }

        try {
            executeAddTorrent(torrentBytes, torrentName, category, tags, ratioLimit);
        } catch (HttpClientErrorException.Forbidden e) {
            log.atInfo().log("qBittorrent session expired (403). Re-authenticating and retrying...");
            authenticate();
            executeAddTorrent(torrentBytes, torrentName, category, tags, ratioLimit);
        }
    }

    private synchronized void authenticate() {
        log.atInfo().log("Authenticating with QBittorrent API...");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", properties.username());
        formData.add("password", properties.password());

        ResponseEntity<Void> response = restClient.post()
                .uri("/api/v2/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toBodilessEntity();

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null ) {
            for (String cookie : cookies) {
                if (cookie.startsWith("SID=")) {
                    String sid = cookie.split(";")[0];
                    authCookie.set(sid);
                    log.info("Successfully authenticated and cached new SID cookie.");
                    return;
                }
            }
        }
        throw new RuntimeException("Failed to extract SID cookie from qBittorrent login response");
    }

    private void executeAddTorrent(byte[] torrentBytes, String torrentName, String category, String tags, String ratioLimit) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("torrents", new ByteArrayResource(torrentBytes) {
            @Override
            public String getFilename() {
                return torrentName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".torrent";
            }
        });

        if (category != null && !category.isBlank()) {
            builder.part("category", category);
        }

        if (tags != null && !tags.isBlank()) {
            builder.part("tags", tags);
        }

        if (ratioLimit != null && !ratioLimit.isBlank()) {
            builder.part("ratioLimit", ratioLimit);
        }

        restClient.post()
                .uri("/api/v2/torrents/add")
                .header(HttpHeaders.COOKIE, authCookie.get())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(builder.build())
                .retrieve()
                .onStatus(
                        status -> status.isSameCodeAs(HttpStatus.FORBIDDEN),
                        (req, res) -> {
                            throw HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden", res.getHeaders(), null, null);
                        })
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {
                            throw new RuntimeException("qBittorrent returned error code: " + res.getStatusCode());
                        })
                .toBodilessEntity();

        log.atInfo().log("Successfully added torrent to QBittorrent: {}", torrentName);
    }
}
