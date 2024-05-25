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

data class FocuserMoveAbsoluteTask(
    override val focuser: Focuser,
    @JvmField val position: Int,
) : FocuserMoveTask, CancellationListener {

    private val latch = CountUpDownLatch()

    override fun handleFocuserEvent(event: FocuserEvent) {
        if (event.device === focuser) {
            when (event) {
                is FocuserPositionChanged -> if (focuser.position == position) latch.reset()
                is FocuserMoveFailed -> latch.reset()
            }
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        if (!cancellationToken.isDone && focuser.connected
            && !focuser.moving && position != focuser.position
        ) {
            try {
                cancellationToken.listen(this)

                LOG.info("Focuser move started. focuser={}, position={}", focuser, position)

                if (focuser.canAbsoluteMove) focuser.moveFocusTo(position)
                else if (focuser.position - position < 0) focuser.moveFocusIn(abs(focuser.position - position))
                else focuser.moveFocusOut(abs(focuser.position - position))

                latch.await()
            } finally {
                cancellationToken.unlisten(this)
            }

            LOG.info("Focuser move finished. focuser={}, position={}", focuser, position)
        }
    }

    override fun onCancel(source: CancellationSource) {
        focuser.abortFocus()
        latch.reset()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FocuserMoveAbsoluteTask>()
    }
}
