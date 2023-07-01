package nebulosa.desktop.logic.filterwheel

import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.indi.device.filterwheel.*
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.beans.factory.annotation.Autowired

data class FilterWheelMoveTask(
    override val filterWheel: FilterWheel,
    val position: Int,
) : FilterWheelTask {

    @Autowired private lateinit var eventBus: EventBus

    private val latch = CountUpDownLatch()

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onEvent(event: FilterWheelEvent) {
        if (event.device !== filterWheel) return

        when (event) {
            is FilterWheelPositionChanged -> latch.countDown()
            is FilterWheelDetached,
            is FilterWheelMoveFailed -> latch.reset()
        }
    }

    override fun call() {
        try {
            eventBus.post(TaskStarted(this))

            if (filterWheel.position != position
                && position in 1..filterWheel.count
            ) {
                synchronized(filterWheel) {
                    eventBus.register(this)

                    latch.countUp()

                    LOG.info("moving filter wheel ${filterWheel.name} to position $position")

                    filterWheel.moveTo(position)

                    latch.await()
                }
            }
        } catch (e: Throwable) {
            LOG.error("filter wheel move failed.", e)
            throw e
        } finally {
            eventBus.unregister(this)
            eventBus.post(TaskFinished(this))
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FilterWheelMoveTask>()
    }
}
