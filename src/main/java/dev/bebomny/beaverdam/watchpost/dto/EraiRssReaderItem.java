package dev.bebomny.beaverdam.watchpost.dto;

import com.apptasticsoftware.rssreader.DateTime;
import com.apptasticsoftware.rssreader.DateTimeParser;
import com.apptasticsoftware.rssreader.Item;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZoneId;

@Getter
@Setter
@ToString
public class EraiRssReaderItem extends Item {

    private Long rssFeedId;

    private String resolution;
    private String infoMKV;
    private String linkType;
    private String size;
    private String infoHash;
    private String subtitles; //"[lang1][lang2][lang3]..."
    private String eraiCategory; //"[category]" -> [Encoded], [Airing]...

    public EraiRssReaderItem() {
        super(new DateTime(ZoneId.systemDefault()));
    }

    public EraiRssReaderItem(DateTimeParser dateTimeParser) {
        super(dateTimeParser);
    }
}
