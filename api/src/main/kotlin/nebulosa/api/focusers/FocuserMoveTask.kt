package nebulosa.api.focusers

import nebulosa.api.tasks.Task
import nebulosa.indi.device.focuser.Focuser

interface FocuserMoveTask : Task, FocuserEventAware {

    val focuser: Focuser
}
