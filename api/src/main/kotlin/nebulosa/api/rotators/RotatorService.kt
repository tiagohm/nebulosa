package nebulosa.api.rotators

import nebulosa.indi.device.rotator.Rotator

class RotatorService(private val rotatorEventHub: RotatorEventHub) {

    fun connect(rotator: Rotator) {
        rotator.connect()
    }

    fun disconnect(rotator: Rotator) {
        rotator.disconnect()
    }

    fun reverse(rotator: Rotator, enabled: Boolean) {
        rotator.reverseRotator(enabled)
    }

    fun move(rotator: Rotator, angle: Double) {
        rotator.moveRotator(angle)
    }

    fun abort(rotator: Rotator) {
        rotator.abortRotator()
    }

    fun sync(rotator: Rotator, angle: Double) {
        rotator.syncRotator(angle)
    }

    fun home(rotator: Rotator) {
        rotator.homeRotator()
    }

    fun listen(rotator: Rotator) {
        rotatorEventHub.listen(rotator)
    }
}
