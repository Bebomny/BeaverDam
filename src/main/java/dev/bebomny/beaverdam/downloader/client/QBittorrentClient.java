package dev.bebomny.beaverdam.downloader.client;

import dev.bebomny.beaverdam.downloader.config.QBittorrentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class QBittorrentClient {

    private static final String USER_AGENT = "BeaverDam - Private Discord Bot";

    private final RestClient restClient;
    private final QBittorrentProperties properties;

    private final AtomicReference<String> authCookie = new AtomicReference<>(null);
    private final Object authLock = new Object();

    /**
     * Adds a torrent to qbittorrent
     *
     * @param torrentBytes the torrent file in Byte Array form
     * @param torrentName  the name of the torrent file
     * @param category     categories to assign to the torrent in qbit
     * @param tags         tags to assign to the torrent split by ','
     * @param ratioLimit   the share ratio limit to set on the torrent. Leave blank for no share limit
     */
    public void addTorrent(byte[] torrentBytes, String torrentName, String category, String tags, String ratioLimit) {
        if (authCookie.get() == null) {
            synchronized (authLock) {
                if (authCookie.get() == null) {
                    authenticate();
                }
            }
        }

        try {
            executeAddTorrent(torrentBytes, torrentName, category, tags, ratioLimit);
        } catch (HttpClientErrorException.Forbidden e) {
            log.atInfo().log("qBittorrent session expired (403). Re-authenticating and retrying...");

            String failedCookie = authCookie.get();
            synchronized (authLock) {
                if (failedCookie != null && failedCookie.equals(authCookie.get())) {
                    authenticate();
                }
            }

            executeAddTorrent(torrentBytes, torrentName, category, tags, ratioLimit);
        }
    }

    private void authenticate() {
        log.atInfo().log("Authenticating with QBittorrent API...");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", properties.username());
        formData.add("password", properties.password());

        ResponseEntity<Void> response = restClient.post()
                .uri("/api/v2/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .body(formData)
                .retrieve()
                .toBodilessEntity();

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith("QBT_SID_8080=")) {
                    String sid = cookie.split(";")[0];
                    authCookie.set(sid);
                    log.atInfo().log("Successfully authenticated and cached new SID cookie. {}", sid);
                    return;
                }
            }
        }
        throw new RuntimeException("Failed to extract SID cookie from qBittorrent login response");
    }

    private void executeAddTorrent(byte[] torrentBytes, String torrentName, String category, String tags, String ratioLimit) {
        try {
            String boundary = "---------------------------" + UUID.randomUUID().toString().replace("-", "");

            Map<String, String> params = new HashMap<>();
            if (category != null && !category.isBlank()) {
                params.put("category", category);
            }
            if (tags != null && !tags.isBlank()) {
                params.put("tags", tags);
            }
            if (ratioLimit != null && !ratioLimit.isBlank()) {
                params.put("ratioLimit", ratioLimit);
            }

            byte[] requestBody = buildRawMultipartBody(boundary, torrentBytes, torrentName, params);

            ResponseEntity<String> response = restClient.post()
                    .uri("/api/v2/torrents/add")
                    .header(HttpHeaders.COOKIE, authCookie.get())
                    .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary)
                    .header(HttpHeaders.REFERER, properties.baseUrl())
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isSameCodeAs(HttpStatus.FORBIDDEN), (_, res) -> {
                        throw HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden", res.getHeaders(), null, null);
                    })
                    .onStatus(HttpStatusCode::isError, (_, res) -> {
                        throw new RuntimeException("qBittorrent returned error code: " + res.getStatusCode());
                    })
                    .toEntity(String.class);

            String responseBody = response.getBody();
            log.atDebug().log("qBittorrent response: '{}'", responseBody);

            if (responseBody != null && responseBody.trim().equalsIgnoreCase("Fails.")) {
                log.atError().log("qBittorrent rejected the torrent '{}'! Returned 'Fails.'", torrentName);

                throw new RuntimeException("qBittorrent failed to add the torrent.");
            } else {
                log.atInfo().log("Successfully added torrent to QBittorrent: {}", torrentName);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to construct multipart body in memory", e);
        }
    }

    private byte[] buildRawMultipartBody(String boundary, byte[] fileBytes, String torrentName, Map<String, String> params) throws IOException {
        byte[] newLine = "\r\n".getBytes();
        byte[] boundaryBytes = ("--" + boundary + "\r\n").getBytes();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                String constructedParam = "Content-Disposition: form-data; name=\"%s\"\r\n\r\n%s\r\n"
                        .formatted(param.getKey(), param.getValue());
                outputStream.write(boundaryBytes);
                outputStream.write(constructedParam.getBytes());
            }

            String safeFilename = torrentName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".torrent";
            String fileHeader = "Content-Disposition: form-data; name=\"torrents\"; filename=\"%s\"\r\nContent-Type: application/x-bittorrent\r\n\r\n"
                    .formatted(safeFilename);

            outputStream.write(boundaryBytes);
            outputStream.write(fileHeader.getBytes());
            outputStream.write(fileBytes);
            outputStream.write(newLine);

            outputStream.write(("--" + boundary + "--\r\n").getBytes());

            return outputStream.toByteArray();
        }
    }
}
