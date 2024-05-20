package nebulosa.indi.device.rotator

import nebulosa.indi.device.DeviceDetached

data class RotatorDetached(override val device: Rotator) : RotatorEvent, DeviceDetached<Rotator>
