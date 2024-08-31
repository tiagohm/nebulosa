package nebulosa.api.focusers

import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.indi.device.focuser.FocuserMoveFailed
import nebulosa.indi.device.focuser.FocuserMovingChanged
import nebulosa.indi.device.focuser.FocuserPositionChanged
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationListener
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.cancellation.CancellationToken
import nebulosa.util.concurrency.latch.CountUpDownLatch

abstract class AbstractFocuserMoveTask : FocuserMoveTask, CancellationListener {

    @JvmField protected val latch = CountUpDownLatch()

    @Volatile private var moving = false

    override fun handleFocuserEvent(event: FocuserEvent) {
        if (event.device === focuser) {
            when (event) {
                is FocuserMovingChanged -> if (event.device.moving) moving = true else latch.reset()
                is FocuserPositionChanged -> if (moving && !event.device.moving) latch.reset()
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
                LOG.info("Focuser move started. focuser={}", focuser)
                latch.countUp()
                move()
                latch.await()
            } finally {
                moving = false
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
