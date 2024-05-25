package nebulosa.api.focusers

import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.indi.device.focuser.FocuserMoveFailed
import nebulosa.indi.device.focuser.FocuserPositionChanged
import nebulosa.log.loggerFor
import kotlin.math.abs

data class FocuserMoveRelativeTask(
    override val focuser: Focuser,
    @JvmField val offset: Int,
) : FocuserMoveTask, CancellationListener {

    private val latch = CountUpDownLatch()

    @Volatile private var initialPosition = 0

    override fun handleFocuserEvent(event: FocuserEvent) {
        if (event.device === focuser) {
            when (event) {
                is FocuserPositionChanged -> if (abs(focuser.position - initialPosition) == abs(offset)) latch.reset()
                is FocuserMoveFailed -> latch.reset()
            }
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        if (!cancellationToken.isDone && focuser.connected && !focuser.moving && offset != 0) {
            try {
                cancellationToken.listen(this)

                initialPosition = focuser.position

                LOG.info("Focuser move started. focuser={}, offset={}", focuser, offset)

                if (!focuser.canRelativeMove) focuser.moveFocusTo(focuser.position + offset)
                else if (offset > 0) focuser.moveFocusOut(offset)
                else focuser.moveFocusIn(offset)

                latch.await()
            } finally {
                cancellationToken.unlisten(this)
            }

            LOG.info("Focuser move finished. focuser={}, offset={}", focuser, offset)
        }
    }

    override fun onCancel(source: CancellationSource) {
        focuser.abortFocus()
        latch.reset()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FocuserMoveRelativeTask>()
    }
}
