package nebulosa.desktop.logic.filterwheel

import nebulosa.desktop.logic.taskexecutor.Task
import nebulosa.indi.device.filterwheels.FilterWheel

interface FilterWheelTask : Task {

    val filterWheel: FilterWheel
}
