package dev.bebomny.beaverdam.watchpost.parsers;

import dev.bebomny.beaverdam.common.helpers.AnimeHelper;
import dev.bebomny.beaverdam.common.helpers.FileFormatter;
import dev.bebomny.beaverdam.watchpost.dto.AnimeMetadata;
import dev.bebomny.beaverdam.watchpost.dto.EraiRssReaderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class EraiMetadataParser implements AnimeMetadataParser<EraiRssReaderItem> {

    //Erai rss feed example:
/////////////////////////////////
//<rss version="2.0">
//    //<channel>
//    //<title>Erai-raws Torrent RSS</title>
//    //<description>RSS Feed for Torrent Clients</description>
//    //<link>https://www.erai-raws.info</link>
//    //<language>en-US</language>
//    //<atom:link href="https://www.erai-raws.info/feed/?res=1080p&type=torrent&token=c454feb742263e7baec851fa98cabb1b" rel="self" type="application/rss+xml"/>
//    //<image>
//    //<url>
//    //https://www.erai-raws.info/wp-content/uploads/2023/04/cropped-ER-2023-32x32.png
//    //</url>
//    //<title>Erai-raws</title>
//    //<link>https://www.erai-raws.info</link>
//    //<width>32</width>
//    //<height>32</height>
//    //</image>
//    //
//    //<item>
//    //<title>
//    //[Torrent] Magic Maker: Isekai Mahou no Tsukurikata - 04 (HEVC) [1080p CR WEBRip HEVC EAC3][us][br][mx][es][sa][fr][de][it][ru][id][my][th][vn][cn][Encoded]
//    //</title>
//    //<link>
//    //https://t.erai-raws.info/Torrent/2025/Winter/Magic Maker/[Erai-raws] Magic Maker - 04 [1080p CR WEBRip HEVC EAC3][MultiSub].mkv.torrent
//    //</link>
//    //<pubDate>Wed, 29 Jan 2025 19:23:20 +0000</pubDate>
//    //<erai:resolution>1080p</erai:resolution>
//    //<erai:infomkv>CR WEBRip HEVC EAC3</erai:infomkv>
//    //<erai:linktype>Torrent</erai:linktype>
//    //<erai:size>450.26MB</erai:size>
//    //<erai:infohash>91c5fa8a08b2cfea29390dddad82554510817748</erai:infohash>
//    //<erai:subtitles>
//    //[us][br][mx][es][sa][fr][de][it][ru][id][my][th][vn][cn]
//    //</erai:subtitles>
//    //<erai:category>[Encoded]</erai:category>
//    //<description>
//    //<a href="https://www.erai-raws.info/encodes/magic-maker-isekai-mahou-no-tsukurikata-04-hevc/">[Erai-raws] Magic Maker - 04 [1080p CR WEBRip HEVC EAC3][MultiSub][DDD1164E].mkv</a> | Subtitles: [us][br][mx][es][sa][fr][de][it][ru][id][my][th][vn][cn] | Size: 450.26MB | InfoHash: 91c5fa8a08b2cfea29390dddad82554510817748 | Categories: [Encoded]
//    //</description>
//    //</item>

    /// //////////////////////////////////////////

    private static final Pattern eraiVideoDetailsPattern = Pattern.compile("\\[(?<GroupName>[^\\]]+)\\]\\s(?<AnimeName>.+?)\\s-\\s(?<EpisodeNumber>\\d+)\\s\\[(?<Resolution>\\d+p)\\s(?<Source>.+?)\\s(?<SourceType>.+?)\\s(?<VideoType>.+?)\\s(?<AudioType>.+?)\\]\\[(?<Subtitles>MultiSub)\\]\\[(?<CRC32>[A-F0-9]{8})\\]");
    private static final Pattern eraiShortVideoDetailsMovieSpecialPattern = Pattern.compile("\\[(?<GroupName>[^\\]]+)\\]\\s(?<AnimeName>.+?)\\s-\\s(?<EpisodeNumber>\\d+|Movie|.+?)\\s(?:\\[|\\()");
    private static final Pattern infoMKVSplitPattern = Pattern.compile("^(?<Source>.+?)\\s(?<SourceType>.+?)\\s(?<VideoType>.+?)\\s(?<AudioType>.+?)$", Pattern.MULTILINE);

    @Override
    public AnimeMetadata parseMetadata(EraiRssReaderItem item) {
        String infoHash = this.parseInfoHash(item);
        String rawTitle = item.getTitle().orElse("Unknown");
        String fileLink = item.getLink().orElse(null);
        String linkType = item.getLinkType();
        String guid = item.getGuid().orElse(null);
        String pubDate = item.getPubDate().orElse("");
        String videoCategory = item.getEraiCategory();

        //VideoDetails
        Matcher videoDetailsMatcher = eraiShortVideoDetailsMovieSpecialPattern.matcher(rawTitle);
        if (!videoDetailsMatcher.find()) {
            log.atWarn().log("Failed to find video details for {} using: {}", rawTitle, eraiShortVideoDetailsMovieSpecialPattern.pattern());
            return null;
        }

        String animeName = videoDetailsMatcher.group("AnimeName");
        String groupName = videoDetailsMatcher.group("GroupName");
        String episodeNumber = videoDetailsMatcher.group("EpisodeNumber");

        Long fileSizeBytes = FileFormatter.parseFileSizeToAmountOfBytes(item.getSize());
        Boolean repack = rawTitle.toLowerCase().contains("repack");

        //FileDetails
        if (item.getInfoMKV() == null) {
            log.atWarn().log("Failed to parse info mkv for {}. InfoMKV is missing!", rawTitle);
            return null;
        }

        Matcher infoMKVMatcher = infoMKVSplitPattern.matcher(item.getInfoMKV());
        if (!infoMKVMatcher.find()) {
            log.atWarn().log("Failed to parse info mkv for {}. Failed to locate pattern!", rawTitle);
            return null;
        }

        String source = infoMKVMatcher.group("Source");
        String sourceType = infoMKVMatcher.group("SourceType");
        String videoType = infoMKVMatcher.group("VideoType");
        String audioType = infoMKVMatcher.group("AudioType");

        String resolution = item.getResolution();
        String subtitles = item.getSubtitles();
        String crc32Checksum = "None"; //Erai doesnt bundle checksums into their rss items

        return AnimeMetadata.builder()
                .infoHash(infoHash)
                .rawTitle(rawTitle)
                .fileLink(fileLink)
                .linkType(linkType)
                .guid(guid)
                .pubDate(AnimeHelper.parseRssPubDate(pubDate))
                .videoCategory(videoCategory)
                .seriesName(animeName)
                .groupName(groupName)
                .episode(episodeNumber)
                .season("Unknown") //TODO parse the season from the title, often completely missing
                .rssCategory("Unknown")
                .fileSizeBytes(fileSizeBytes)
                .repack(repack)
                .resolution(resolution)
                .source(source)
                .sourceType(sourceType)
                .videoType(videoType)
                .audioType(audioType)
                .subtitles(subtitles)
                .crc32Checksum(crc32Checksum)
                .build();

//        return new AnimeMetadata(
//                infoHash,
//                rawTitle,
//                fileLink,
//                linkType,
//                guid,
//                AnimeHelper.parseRssPubDate(pubDate),
//                videoCategory,
//                animeName,
//                groupName,
//                episodeNumber,
//                "Unknown",
//                "Unknown",
//                fileSizeBytes,
//                repack,
//                resolution,
//                source,
//                sourceType,
//                videoType,
//                audioType,
//                subtitles,
//                crc32Checksum
//        );
    }

    @Override
    public String parseInfoHash(EraiRssReaderItem item) {
        if (item.getInfoHash() == null || item.getInfoHash().isBlank()) {
            return AnimeHelper.generateSyntheticHash(item.getTitle().orElse(""));
        }
        return item.getInfoHash();
    }
}
