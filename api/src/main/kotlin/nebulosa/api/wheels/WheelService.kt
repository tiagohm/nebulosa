package nebulosa.api.wheels

import nebulosa.indi.device.filterwheel.FilterWheel

class WheelService(private val wheelEventHub: WheelEventHub) {

    fun connect(wheel: FilterWheel) {
        wheel.connect()
    }

    fun disconnect(wheel: FilterWheel) {
        wheel.disconnect()
    }

    fun moveTo(wheel: FilterWheel, steps: Int) {
        wheel.moveTo(steps)
    }

    fun sync(wheel: FilterWheel, names: Iterable<String>) {
        wheel.names(names)
    }

    fun listen(wheel: FilterWheel) {
        wheelEventHub.listen(wheel)
    }
}
