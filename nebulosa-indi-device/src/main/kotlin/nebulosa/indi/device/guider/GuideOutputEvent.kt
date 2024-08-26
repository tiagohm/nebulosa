package nebulosa.indi.device.guider

import nebulosa.indi.device.DeviceEvent

interface GuideOutputEvent : DeviceEvent<GuideOutput> {

    override val device: GuideOutput
}
