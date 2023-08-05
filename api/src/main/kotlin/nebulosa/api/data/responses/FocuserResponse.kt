package nebulosa.api.data.responses

import nebulosa.indi.device.focuser.Focuser

data class FocuserResponse(
    val name: String,
    val connected: Boolean,
    val moving: Boolean,
    val position: Int,
    val canAbsoluteMove: Boolean,
    val canRelativeMove: Boolean,
    val canAbort: Boolean,
    val canReverse: Boolean,
    val reverse: Boolean,
    val canSync: Boolean,
    val hasBacklash: Boolean,
    val maxPosition: Int,
    val hasThermometer: Boolean,
    val temperature: Double,
) {

    constructor(focuser: Focuser) : this(
        focuser.name,
        focuser.connected,
        focuser.moving,
        focuser.position,
        focuser.canAbsoluteMove,
        focuser.canRelativeMove,
        focuser.canAbort,
        focuser.canReverse,
        focuser.reverse,
        focuser.canSync,
        focuser.hasBacklash,
        focuser.maxPosition,
        focuser.hasThermometer,
        focuser.temperature,
    )
}
