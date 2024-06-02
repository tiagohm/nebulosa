package nebulosa.api.cameras

import nebulosa.api.devices.DeviceMessageEvent
import nebulosa.indi.device.camera.Camera

data class CameraMessageEvent(override val eventName: String, override val device: Camera) : DeviceMessageEvent<Camera>
