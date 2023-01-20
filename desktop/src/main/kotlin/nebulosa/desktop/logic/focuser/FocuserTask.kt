package nebulosa.desktop.logic.focuser

import nebulosa.desktop.logic.task.Task
import nebulosa.indi.device.focusers.Focuser

interface FocuserTask : Task {

    val focuser: Focuser
}
