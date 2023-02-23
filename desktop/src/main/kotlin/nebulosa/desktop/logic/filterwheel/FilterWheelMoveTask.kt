package nebulosa.desktop.logic.filterwheel

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.logic.DeviceEventBus
import nebulosa.desktop.logic.TaskEventBus
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.filterwheel.FilterWheelMoveFailed
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

data class FilterWheelMoveTask(
    override val filterWheel: FilterWheel,
    val position: Int,
) : FilterWheelTask {

    @Autowired private lateinit var deviceEventBus: DeviceEventBus
    @Autowired private lateinit var taskEventBus: TaskEventBus

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
            taskEventBus.onNext(TaskStarted(this))

            if (filterWheel.position != position
                && position in 1..filterWheel.count
            ) {
                synchronized(filterWheel) {
                    latch.countUp()

                    subscriber = deviceEventBus
                        .filter { it.device === filterWheel }
                        .subscribe(::onEvent)

                    LOG.info("moving filter wheel ${filterWheel.name} to position $position")

                    filterWheel.moveTo(position)

                    latch.await()
                }
            }
        } finally {
            subscriber?.dispose()

            taskEventBus.onNext(TaskFinished(this))
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(FilterWheelMoveTask::class.java)
    }
}
