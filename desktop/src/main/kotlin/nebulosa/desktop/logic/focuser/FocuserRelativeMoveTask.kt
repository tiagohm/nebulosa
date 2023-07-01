package nebulosa.desktop.logic.focuser

import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.indi.device.focuser.*
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.beans.factory.annotation.Autowired

data class FocuserRelativeMoveTask(
    override val focuser: Focuser,
    val increment: Int,
    val direction: FocuserDirection,
) : FocuserTask {

    @Autowired private lateinit var eventBus: EventBus

    private val latch = CountUpDownLatch()

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onEvent(event: FocuserEvent) {
        if (event.device !== focuser) return

        when (event) {
            is FocuserMovingChanged -> if (!event.device.moving) latch.countDown()
            is FocuserDetached,
            is FocuserMoveFailed -> latch.reset()
        }
    }

    override fun call() {
        try {
            eventBus.post(TaskStarted(this))

            if (increment >= 0 && increment <= focuser.maxPosition) {
                synchronized(focuser) {
                    eventBus.register(this)

                    latch.countUp()

                    LOG.info("moving focuser ${focuser.name} to position by $increment [{}]", direction)

                    if (direction == FocuserDirection.OUT) focuser.moveFocusOut(increment)
                    else focuser.moveFocusIn(increment)

                    latch.await()
                }
            } else {
                LOG.warn("increment is out of range. increment={}, min=0.0, max={}", increment, focuser.maxPosition)
            }
        } catch (e: Throwable) {
            LOG.error("focuser relative move failed.", e)
            throw e
        } finally {
            eventBus.unregister(this)
            eventBus.post(TaskFinished(this))
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FocuserRelativeMoveTask>()
    }
}
