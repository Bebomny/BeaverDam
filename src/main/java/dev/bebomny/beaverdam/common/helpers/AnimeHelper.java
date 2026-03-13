package dev.bebomny.beaverdam.common.helpers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AnimeHelper {
    public static String getReleaseSeasonForDate(LocalDateTime date) {
        int year = date.getYear();
        String seasonString = switch (date.getMonthValue()) {
            case 1,2,3 -> "Winter";
            case 4,5,6 -> "Spring";
            case 7,8,9 -> "Summer";
            case 10,11,12 -> "Fall";
            default -> throw new IllegalStateException("Unexpected value: " + date.getMonthValue());
        };
        return seasonString + " " + year;
    }

    public static LocalDateTime parseRssPubDate(String pubDate) {
        ZonedDateTime zdt = ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);

        return zdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String encodeURL(String url) {
        String pathPart = url.substring(0, url.lastIndexOf("/") + 1);
        String showPart = url.replace(pathPart, "");
        String completeUrl = pathPart + URLEncoder.encode(showPart, StandardCharsets.UTF_8);
        completeUrl = completeUrl.replace("+", "%20");
        completeUrl = completeUrl.replace(" ", "%20");
//        DiscordDebugLogger.debug("[Erai] Original URL: %s", url);
//        DiscordDebugLogger.debug("[Erai] Updated URL: %s", completeUrl);
        return completeUrl;
    }

    /**
     * Generates a 40-character SHA-1 hash from the RSS item's raw title.
     * Used only when the info hash is missing from the rss item.
     */
    public static String generateSyntheticHash(String rawTitle) {
        if (rawTitle == null || rawTitle.isBlank()) {
            throw new IllegalArgumentException("Cannot generate hash from empty title");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encodedHash = digest.digest(rawTitle.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(encodedHash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
