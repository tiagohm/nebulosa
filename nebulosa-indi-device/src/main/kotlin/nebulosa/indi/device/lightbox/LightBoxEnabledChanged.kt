package nebulosa.indi.device.lightbox

import nebulosa.indi.device.PropertyChangedEvent

data class LightBoxEnabledChanged(override val device: LightBox) : LightBoxEvent, PropertyChangedEvent
