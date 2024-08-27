package nebulosa.indi.device.lightbox

import nebulosa.indi.device.DeviceEvent

interface LightBoxEvent : DeviceEvent<LightBox> {

    override val device: LightBox
}
