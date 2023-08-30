package nebulosa.api.wheels

import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.stereotype.Service
import java.util.*

@Service
class WheelService : Vector<FilterWheel>(2) {

    operator fun get(name: String): FilterWheel? {
        return firstOrNull { it.name == name }
    }

    fun connect(filterWheel: FilterWheel) {
        filterWheel.connect()
    }

    fun disconnect(filterWheel: FilterWheel) {
        filterWheel.disconnect()
    }

    fun moveTo(filterWheel: FilterWheel, steps: Int) {
        filterWheel.moveTo(steps)
    }

    fun syncNames(filterWheel: FilterWheel, filterNames: List<String>) {
        filterWheel.syncNames(filterNames)
    }
}
