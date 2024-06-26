package nebulosa.api.focusers

import nebulosa.indi.device.focuser.Focuser
import kotlin.math.abs

data class FocuserMoveAbsoluteTask(
    override val focuser: Focuser,
    @JvmField @Volatile var position: Int,
) : AbstractFocuserMoveTask() {

    override fun canMove() = position != focuser.position && position > 0 && position < focuser.maxPosition

    override fun move() {
        if (focuser.canAbsoluteMove) focuser.moveFocusTo(position)
        else if (position < focuser.position) focuser.moveFocusIn(abs(position - focuser.position))
        else focuser.moveFocusOut(abs(position - focuser.position))
    }
}
