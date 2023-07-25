package nebulosa.indi.device.camera

import nebulosa.indi.device.DeviceAttached

data class CameraAttached(override val device: Camera) : CameraEvent, DeviceAttached<Camera>
