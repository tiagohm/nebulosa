package nebulosa.indi.devices.events

import nebulosa.indi.devices.cameras.Camera

data class CameraTemperatureChangedEvent(
    override val device: Camera,
    val temperature: Double,
) : CameraEvent
