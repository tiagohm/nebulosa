package nebulosa.indi.device.dustcap

import nebulosa.indi.device.PropertyChangedEvent

data class DustCapParkChanged(override val device: DustCap) : DustCapEvent, PropertyChangedEvent
