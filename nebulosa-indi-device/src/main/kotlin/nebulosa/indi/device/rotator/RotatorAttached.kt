package nebulosa.indi.device.rotator

import nebulosa.indi.device.DeviceAttached

data class RotatorAttached(override val device: Rotator) : RotatorEvent, DeviceAttached<Rotator>
