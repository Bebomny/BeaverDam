package dev.bebomny.beaverdam.downloader;

public interface DownloaderCommandApi {
    void downloadTorrentFromUrl(String url, String category, String tags, String customShareRatio);
}
