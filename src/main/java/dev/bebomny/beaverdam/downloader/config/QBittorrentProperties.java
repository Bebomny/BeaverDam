package dev.bebomny.beaverdam.downloader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qbit.api")
public record QBittorrentProperties(String baseUrl, String username, String password) {
}
