package nebulosa.api.focusers

import nebulosa.indi.device.focuser.Focuser
import nebulosa.job.manager.Task

sealed interface FocuserTask : Task, FocuserEventAware {

    val focuser: Focuser
}
