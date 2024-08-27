package nebulosa.indi.device.lightbox

import nebulosa.indi.device.DeviceAttached

data class LightBoxAttached(override val device: LightBox) : LightBoxEvent, DeviceAttached<LightBox>
