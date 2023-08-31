package nebulosa.api.wheels

import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.stereotype.Service

@Service
class WheelService {

    fun connect(wheel: FilterWheel) {
        wheel.connect()
    }

    fun disconnect(wheel: FilterWheel) {
        wheel.disconnect()
    }

    fun moveTo(wheel: FilterWheel, steps: Int) {
        wheel.moveTo(steps)
    }

    fun syncNames(wheel: FilterWheel, filterNames: List<String>) {
        wheel.syncNames(filterNames)
    }
}