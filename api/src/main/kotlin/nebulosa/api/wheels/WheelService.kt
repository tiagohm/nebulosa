package nebulosa.api.wheels

import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.stereotype.Service

@Service
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

    fun sync(wheel: FilterWheel, names: List<String>) {
        wheel.names(names)
    }

    fun listen(wheel: FilterWheel) {
        wheelEventHub.listen(wheel)
    }
}
