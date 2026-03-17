package dev.bebomny.beaverdam.common.helpers;

public enum ShowSeriesParam {
    IGNORED,
    INTERESTING,
    AUTODOWNLOAD;

    public static ShowSeriesParam from(String stringParam) {
        for (ShowSeriesParam showSeriesParam : ShowSeriesParam.values()) {
            if (showSeriesParam.name().equalsIgnoreCase(stringParam)) {
                return showSeriesParam;
            }
        }
        return null;
    }
}
