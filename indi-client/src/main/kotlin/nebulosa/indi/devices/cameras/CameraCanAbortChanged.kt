package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraCanAbortChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
