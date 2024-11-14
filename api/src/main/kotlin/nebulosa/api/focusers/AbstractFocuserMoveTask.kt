package nebulosa.api.focusers

import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.indi.device.focuser.FocuserMoveFailed
import nebulosa.indi.device.focuser.FocuserMovingChanged
import nebulosa.indi.device.focuser.FocuserPositionChanged
import nebulosa.job.manager.Job
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationListener
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.CountUpDownLatch

sealed class AbstractFocuserMoveTask : FocuserTask, CancellationListener {

    abstract val job: Job

    @JvmField protected val latch = CountUpDownLatch()

    @Volatile private var moving = false

    override fun handleFocuserEvent(event: FocuserEvent) {
        if (event.device === focuser) {
            when (event) {
                is FocuserMovingChanged -> if (event.device.moving) moving = true else if (moving) latch.reset()
                is FocuserPositionChanged -> if (moving && !event.device.moving) latch.reset()
                is FocuserMoveFailed -> latch.reset()
            }
        }
    }

    protected abstract fun canMove(): Boolean

    protected abstract fun move()

    override fun run() {
        if (!job.isCancelled && focuser.connected && !focuser.moving && canMove()) {
            LOG.d { debug("Focuser move started. focuser={}", focuser) }
            latch.countUp()
            moving = true
            move()
            latch.await()
            moving = false
            LOG.d { debug("Focuser move finished. focuser={}", focuser) }
        }
    }

    override fun onCancel(source: CancellationSource) {
        focuser.abortFocus()
        latch.reset()
    }

    companion object {

        private val LOG = loggerFor<AbstractFocuserMoveTask>()
    }
}
