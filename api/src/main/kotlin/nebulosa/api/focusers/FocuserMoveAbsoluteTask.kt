package nebulosa.api.focusers

import nebulosa.indi.device.focuser.Focuser
import nebulosa.job.manager.Job
import kotlin.math.abs

data class FocuserMoveAbsoluteTask(
    override val job: Job,
    override val focuser: Focuser,
    @JvmField val position: Int,
) : AbstractFocuserMoveTask() {

    override fun canMove() = position != focuser.position && position in 0..focuser.maxPosition

    override fun move() {
        if (focuser.canAbsoluteMove) focuser.moveFocusTo(position)
        else if (position < focuser.position) focuser.moveFocusIn(abs(position - focuser.position))
        else focuser.moveFocusOut(abs(position - focuser.position))
    }
}
