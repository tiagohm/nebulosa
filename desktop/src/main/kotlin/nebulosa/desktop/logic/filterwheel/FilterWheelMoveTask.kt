package nebulosa.desktop.logic.filterwheel

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.logic.EventBus
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.filterwheel.FilterWheelMoveFailed
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import org.slf4j.LoggerFactory

data class FilterWheelMoveTask(
    override val filterWheel: FilterWheel,
    val position: Int,
) : FilterWheelTask {

    private val latch = CountUpDownLatch()

    private fun onEvent(event: DeviceEvent<*>) {
        when (event) {
            is FilterWheelPositionChanged -> latch.countDown()
            is FilterWheelDetached,
            is FilterWheelMoveFailed -> latch.reset()
        }
    }

    override fun call() {
        var subscriber: Disposable? = null

        try {
            EventBus.TASK.post(TaskStarted(this))

            if (filterWheel.position != position
                && position in 1..filterWheel.count
            ) {
                synchronized(filterWheel) {
                    latch.countUp()

                    subscriber = EventBus.DEVICE
                        .subscribe(filter = { it.device === filterWheel }, next = ::onEvent)

                    LOG.info("moving filter wheel ${filterWheel.name} to position $position")

                    filterWheel.moveTo(position)

                    latch.await()
                }
            }
        } finally {
            subscriber?.dispose()

            EventBus.TASK.post(TaskFinished(this))
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(FilterWheelMoveTask::class.java)
    }
}
