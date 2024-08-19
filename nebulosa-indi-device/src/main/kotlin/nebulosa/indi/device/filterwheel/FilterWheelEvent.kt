package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.DeviceEvent

interface FilterWheelEvent : DeviceEvent<FilterWheel> {

    override val device: FilterWheel
}
