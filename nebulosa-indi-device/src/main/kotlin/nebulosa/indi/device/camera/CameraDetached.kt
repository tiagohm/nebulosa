package nebulosa.indi.device.camera

import nebulosa.indi.device.DeviceDetached

data class CameraDetached(override val device: Camera) : CameraEvent, DeviceDetached<Camera>
