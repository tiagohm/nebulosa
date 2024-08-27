package nebulosa.indi.device.lightbox

import nebulosa.indi.device.PropertyChangedEvent

data class LightBoxIntensityChanged(override val device: LightBox) : LightBoxEvent, PropertyChangedEvent
