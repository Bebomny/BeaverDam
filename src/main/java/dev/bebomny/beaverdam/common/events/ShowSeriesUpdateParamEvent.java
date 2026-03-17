package dev.bebomny.beaverdam.common.events;

import dev.bebomny.beaverdam.common.helpers.ShowSeriesParam;

public record ShowSeriesUpdateParamEvent(Long targetItemId, ShowSeriesParam param, Boolean newValue) {

}
