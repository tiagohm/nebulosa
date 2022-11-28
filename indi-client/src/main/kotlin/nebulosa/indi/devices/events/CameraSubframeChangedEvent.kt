package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraSubframeChangedEvent(
    override val device: Camera,
    val x: Int, val y: Int, val width: Int, val height: Int,
) : CameraEvent
