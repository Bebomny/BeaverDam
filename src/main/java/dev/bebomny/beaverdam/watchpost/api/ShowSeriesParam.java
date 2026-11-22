package dev.bebomny.beaverdam.watchpost.api;

public enum ShowSeriesParam {
    IGNORED,
    INTERESTING,
    AUTODOWNLOAD,
    SUBMITTED;

    public static ShowSeriesParam from(String stringParam) {
        for (ShowSeriesParam showSeriesParam : ShowSeriesParam.values()) {
            if (showSeriesParam.name().equalsIgnoreCase(stringParam)) {
                return showSeriesParam;
            }
        }
        return null;
    }
}
