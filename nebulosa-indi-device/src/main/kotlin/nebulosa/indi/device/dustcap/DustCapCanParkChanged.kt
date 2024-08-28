package nebulosa.indi.device.dustcap

import nebulosa.indi.device.PropertyChangedEvent

data class DustCapCanParkChanged(override val device: DustCap) : DustCapEvent, PropertyChangedEvent
