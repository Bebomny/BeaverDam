package dev.bebomny.beaverdam.common.helpers;

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
}
