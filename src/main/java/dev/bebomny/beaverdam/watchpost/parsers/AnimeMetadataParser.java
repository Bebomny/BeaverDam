package dev.bebomny.beaverdam.watchpost.parsers;

import com.apptasticsoftware.rssreader.Item;
import dev.bebomny.beaverdam.watchpost.dto.AnimeMetadata;

public interface AnimeMetadataParser<T extends Item> {
    AnimeMetadata parseMetadata(T item);
    String parseInfoHash(T item);
}
