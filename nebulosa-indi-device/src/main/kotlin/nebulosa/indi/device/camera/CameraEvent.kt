package nebulosa.indi.device.camera

import nebulosa.indi.device.DeviceEvent

interface CameraEvent : DeviceEvent<Camera> {

    override val device: Camera
}
