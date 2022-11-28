package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraSubframeMinMaxEvent(
    override val device: Camera,
    val minX: Int, val maxX: Int, val minY: Int, val maxY: Int,
    val minWidth: Int, val maxWidth: Int, val minHeight: Int, val maxHeight: Int,
) : CameraEvent
