package dev.bebomny.beaverdam.discord.helpers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FormatHelper {

    public static String formatSecondsToTimeString(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%dh %02dm %02ds", hours, minutes, seconds);
    }

    public static String formatName(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ");
    }

    public static String encodeUrl(String url) {
        String pathPart = url.substring(0, url.lastIndexOf("/") + 1);
        String showPart = url.replace(pathPart, "");
        String completeUrl = pathPart + URLEncoder.encode(showPart, StandardCharsets.UTF_8);
        completeUrl = completeUrl.replace("+", "%20");
        completeUrl = completeUrl.replace(" ", "%20");
        return completeUrl;
    }
}
