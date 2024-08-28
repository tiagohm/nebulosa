package nebulosa.api.lightboxes

import nebulosa.api.devices.DeviceMessageEvent
import nebulosa.indi.device.lightbox.LightBox

data class LightBoxMessageEvent(override val eventName: String, override val device: LightBox) : DeviceMessageEvent<LightBox>
