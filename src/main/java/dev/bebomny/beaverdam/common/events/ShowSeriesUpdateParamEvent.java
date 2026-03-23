package dev.bebomny.beaverdam.common.events;

import dev.bebomny.beaverdam.common.helpers.ShowSeriesParam;

//TODO: move from the event to a synchronous api
public record ShowSeriesUpdateParamEvent(Long targetItemId, ShowSeriesParam param, Boolean newValue) {

}
