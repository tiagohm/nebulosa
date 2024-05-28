package nebulosa.api.focusers

import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.indi.device.focuser.FocuserMoveFailed
import nebulosa.indi.device.focuser.FocuserPositionChanged
import nebulosa.log.loggerFor

abstract class AbstractFocuserMoveTask : FocuserMoveTask, CancellationListener {

    @JvmField protected val latch = CountUpDownLatch()

    @Volatile private var initialPosition = 0

    override fun handleFocuserEvent(event: FocuserEvent) {
        if (event.device === focuser) {
            when (event) {
                is FocuserPositionChanged -> if (focuser.position != initialPosition && !focuser.moving) latch.reset()
                is FocuserMoveFailed -> latch.reset()
            }
        }
    }

    protected abstract fun canMove(): Boolean

    protected abstract fun move()

    override fun execute(cancellationToken: CancellationToken) {
        if (!cancellationToken.isCancelled && focuser.connected && !focuser.moving && canMove()) {
            try {
                cancellationToken.listen(this)
                initialPosition = focuser.position
                LOG.info("Focuser move started. focuser={}", focuser)
                latch.countUp()
                move()
                latch.await()
            } finally {
                cancellationToken.unlisten(this)
                LOG.info("Focuser move finished. focuser={}", focuser)
            }
        }
    }

    override fun onCancel(source: CancellationSource) {
        focuser.abortFocus()
        latch.reset()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AbstractFocuserMoveTask>()
    }
}
