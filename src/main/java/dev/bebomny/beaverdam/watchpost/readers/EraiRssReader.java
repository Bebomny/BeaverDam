package dev.bebomny.beaverdam.watchpost.readers;

import com.apptasticsoftware.rssreader.AbstractRssReader;
import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.DateTimeParser;
import dev.bebomny.beaverdam.watchpost.dto.EraiRssReaderItem;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
public class EraiRssReader extends AbstractRssReader<Channel, EraiRssReaderItem> {

    private static final String USER_AGENT = "BeaverBot - Qol Private Discord Bot - made by @Bebomny | Contact: contact@mail.sublimeseal.com";

    public EraiRssReader() {
        super();
        initializeHeaders();
    }

    public EraiRssReader(HttpClient httpClient) {
        super(httpClient);
        initializeHeaders();
    }

    private void initializeHeaders() {
        addHeader("User-Agent", USER_AGENT);
    }

    @Override
    protected void registerItemTags() {
        super.registerItemTags();
        addItemExtension("erai:resolution", EraiRssReaderItem::setResolution);
        addItemExtension("erai:infomkv", EraiRssReaderItem::setInfoMKV);
        addItemExtension("erai:linktype", EraiRssReaderItem::setLinkType);
        addItemExtension("erai:size", EraiRssReaderItem::setSize);
        addItemExtension("erai:infohash", EraiRssReaderItem::setInfoHash);
        addItemExtension("erai:subtitles", EraiRssReaderItem::setSubtitles);
        addItemExtension("erai:category", EraiRssReaderItem::setEraiCategory);
    }

    @Override
    protected Channel createChannel(DateTimeParser dateTimeParser) {
        return new Channel(dateTimeParser);
    }

    @Override
    protected EraiRssReaderItem createItem(DateTimeParser dateTimeParser) {
        return new EraiRssReaderItem(dateTimeParser);
    }
}
