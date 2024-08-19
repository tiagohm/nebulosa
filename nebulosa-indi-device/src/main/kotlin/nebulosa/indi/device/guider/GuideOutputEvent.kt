package nebulosa.indi.device.guider

import nebulosa.indi.device.DeviceEvent

interface GuideOutputEvent<T : GuideOutput> : DeviceEvent<T> {

    override val device: T
}
