package nebulosa.api.data.responses

import nebulosa.indi.device.filterwheel.FilterWheel

data class FilterWheelResponse(
    val name: String,
    val connected: Boolean,
    val count: Int,
    val position: Int,
    val moving: Boolean,
) {

    constructor(filterWheel: FilterWheel) : this(
        filterWheel.name,
        filterWheel.connected,
        filterWheel.count,
        filterWheel.position,
        filterWheel.moving,
    )
}
