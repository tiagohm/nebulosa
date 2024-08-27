package nebulosa.indi.device.lightbox

import nebulosa.indi.device.DeviceDetached

data class LightBoxDetached(override val device: LightBox) : LightBoxEvent, DeviceDetached<LightBox>
