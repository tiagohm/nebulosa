package nebulosa.indi.device.rotator

import nebulosa.indi.device.DeviceEvent

interface RotatorEvent : DeviceEvent<Rotator> {

    override val device: Rotator
}
