package dev.bebomny.beaverdam.downloader.services;

import dev.bebomny.beaverdam.downloader.DownloaderCommandApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloaderCommandServiceImpl implements DownloaderCommandApi {

    private final TorrentManagerService torrentManagerService;

    @Override
    public void downloadTorrentFromUrl(String url, String category, String tags, String customShareRatio) {
        torrentManagerService.processDownload(url, "custom_torrent", category, tags, customShareRatio);
    }
}
