package nebulosa.api.focusers

import nebulosa.indi.device.focuser.Focuser
import kotlin.math.abs

data class FocuserMoveRelativeTask(
    override val focuser: Focuser,
    @JvmField val offset: Int,
) : AbstractFocuserMoveTask() {

    override fun canMove() = offset != 0

    override fun move() {
        if (!focuser.canRelativeMove) focuser.moveFocusTo(focuser.position + offset)
        else if (offset > 0) focuser.moveFocusOut(offset)
        else focuser.moveFocusIn(abs(offset))
    }
}
