package nebulosa.desktop.logic.focuser

import nebulosa.desktop.logic.task.Task
import nebulosa.indi.device.focuser.Focuser

interface FocuserTask : Task<Unit> {

    val focuser: Focuser
}
