package nebulosa.api.dustcap

import nebulosa.indi.device.dustcap.DustCapEvent

fun interface DustCapEventAware {

    fun handleDustCapEvent(event: DustCapEvent)
}
