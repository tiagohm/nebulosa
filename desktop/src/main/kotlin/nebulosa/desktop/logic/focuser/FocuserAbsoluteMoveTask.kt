package nebulosa.desktop.logic.focuser

import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.indi.device.focuser.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

data class FocuserAbsoluteMoveTask(
    override val focuser: Focuser,
    val position: Int,
) : FocuserTask {

    @Autowired private lateinit var eventBus: EventBus

    private val latch = CountUpDownLatch()

    @Subscribe
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

            if (position >= 0 && position <= focuser.maxPosition) {
                synchronized(focuser) {
                    eventBus.register(this)

                    latch.countUp()

                    LOG.info("moving focuser ${focuser.name} to absolute position $position")

                    focuser.moveFocusTo(position)

                    latch.await()
                }
            }
        } catch (e: Throwable) {
            LOG.error("focuser absolute move failed.", e)
            throw e
        } finally {
            eventBus.unregister(this)
            eventBus.post(TaskFinished(this))
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(FocuserAbsoluteMoveTask::class.java)
    }
}
