package dev.bebomny.beaverdam.downloader.events;

/**
 * Request to download a torrent file and submit it to the torrent client.
 *
 * @param referenceId      domain identifier of the item being downloaded (e.g. animeItemId)
 * @param torrentUrl       URL of the .torrent file
 * @param torrentName      display name to assign to the torrent
 * @param category         qBittorrent category
 * @param tags             comma-separated qBittorrent tags
 * @param customShareRatio optional seed ratio limit
 */
public record TorrentDownloadRequestEvent(
        Long referenceId,
        String torrentUrl,
        String torrentName,
        String category,
        String tags,
        String customShareRatio
) {
}
