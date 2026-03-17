package dev.bebomny.beaverdam.downloader.services;

import dev.bebomny.beaverdam.downloader.client.QBittorrentClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Slf4j
@Service
public class TorrentManagerService {

    private final QBittorrentClient qBittorrentClient;
    private final RestClient webClient;

    public TorrentManagerService(QBittorrentClient qBittorrentClient, @Qualifier("vpnHttpClient") HttpClient vpnHttpClient) {
        this.qBittorrentClient = qBittorrentClient;

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(vpnHttpClient);
        this.webClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public void processDownload(String torrentUrl, String torrentName, String category, String tags, String ratioLimit) {
        try {
            log.atInfo().log("Downloading .torrent file from: {}", torrentUrl);

            byte[] torrentFileBytes = webClient.get()
                    .uri(torrentUrl)
                    .retrieve()
                    .body(byte[].class);

            if (torrentFileBytes == null || torrentFileBytes.length == 0) {
                throw new RuntimeException("Downloaded torrent file is empty!");
            }

            log.atInfo().log("Successfully downloaded {} bytes. Sending to qbittorrent...", torrentFileBytes.length);

            qBittorrentClient.addTorrent(torrentFileBytes, torrentName, category, tags, ratioLimit);
        } catch (Exception e) {
            log.error("Failed to process torrent download for '{}': {}", torrentName, e.getMessage());

            throw new RuntimeException("Torrent processing failed", e);
        }
    }
}
