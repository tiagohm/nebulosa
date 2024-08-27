package nebulosa.indi.device.lightbox

import nebulosa.indi.device.PropertyChangedEvent

data class LightBoxIntensityMinMaxChanged(override val device: LightBox) : LightBoxEvent, PropertyChangedEvent
