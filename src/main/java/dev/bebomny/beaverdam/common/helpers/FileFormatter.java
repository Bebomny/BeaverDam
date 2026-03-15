package dev.bebomny.beaverdam.common.helpers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFormatter {
    public static long parseFileSizeToAmountOfBytes(String size) {
        if (size == null || size.trim().isEmpty()) {
            return -1L;
        }

        Pattern pattern = Pattern.compile("^([0-9.,]+)\\s*([a-zA-Z]*)$");
        Matcher matcher = pattern.matcher(size.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid file size format: " + size);
        }

        String numericPart = matcher.group(1).replace(",", "");
        double value = Double.parseDouble(numericPart);
        String unit = matcher.group(2).toUpperCase();

        long multiplier = switch (unit) {
            case "", "B" -> 1L;
            case "KB", "KIB" -> 1024L;
            case "MB", "MIB" -> 1024L * 1024L;
            case "GB", "GIB" -> 1024L * 1024L * 1024L;
            case "TB", "TIB" -> 1024L * 1024L * 1024L * 1024L;
            case "PB", "PIB" -> 1024L * 1024L * 1024L * 1024L * 1024L;
            default -> throw new IllegalArgumentException("Unknown file size unit: " + unit);
        };

        return (long) (value * multiplier);
    }

    public static String parseBytesToFileSizeString(long fileSizeBytes) {
        if (fileSizeBytes == -1L) {
            //This is so I can spot if there were some parsing issues, the latter check takes care of larger discrepancies
            return "-1B";
        }
        if (fileSizeBytes == 0) {
            return "0B";
        }
        if (fileSizeBytes < 0) {
            throw new IllegalArgumentException("File size cannot be negative: " + fileSizeBytes);
        }

        String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB"}; // Future proofing, lets download the whole internet baby

        int unitIndex = (int) (Math.log(fileSizeBytes) / Math.log(1024));
        unitIndex = Math.min(unitIndex, units.length - 1);
        double value = fileSizeBytes / Math.pow(1024, unitIndex);

        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

        return df.format(value) + units[unitIndex];
    }
}
