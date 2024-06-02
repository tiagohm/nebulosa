package nebulosa.api.rotators

import nebulosa.indi.device.rotator.RotatorEvent

fun interface RotatorEventAware {

    fun handleRotatorEvent(event: RotatorEvent)
}
