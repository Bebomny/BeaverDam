package dev.bebomny.beaverdam.common.helpers;

public class AnsiColorUtils {
    // ANSI Color codes
    public static final char ANSI_ESCAPE_CHAR = '\u001B';
    public static final String INFO_COLOR = getANSIColorCode(0, 36); // Blue 34 / Cyan 36
    public static final String WARN_COLOR = getANSIColorCode(0, 33); // Yellow (Orange)
    public static final String ERROR_COLOR = getANSIColorCode(0, 31); // Red
    public static final String DEBUG_COLOR = getANSIColorCode(0, 32); // Green <-- This is useless
    public static final String AUTOPAUSE_COLOR = getANSIColorCode(0, 35); // Pink
    public static final String DEFAULT_COLOR = ""; // No color

    public static String getANSIColorCode(int format, int color) {
        return ANSI_ESCAPE_CHAR + ("[%d;%dm".formatted(format, color));
    }
}
