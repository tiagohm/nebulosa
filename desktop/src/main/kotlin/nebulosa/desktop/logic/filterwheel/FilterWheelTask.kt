package nebulosa.desktop.logic.filterwheel

import nebulosa.desktop.logic.task.Task
import nebulosa.indi.device.filterwheel.FilterWheel

sealed interface FilterWheelTask : Task<Unit> {

    val filterWheel: FilterWheel
}
