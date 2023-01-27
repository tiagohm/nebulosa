package nebulosa.desktop.logic.filterwheel

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.indi.device.filterwheels.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

data class FilterWheelMoveTask(
    override val filterWheel: FilterWheel,
    val position: Int,
) : FilterWheelTask, KoinComponent {

    private val eventBus by inject<EventBus>()
    private val latch = CountUpDownLatch()

    private fun onFilterWheelEvent(event: FilterWheelEvent) {
        when (event) {
            is FilterWheelPositionChanged -> latch.countDown()
            is FilterWheelDetached,
            is FilterWheelMoveFailed -> {
                latch.reset()
                closeGracefully()
            }
        }
    }

    override fun call(): Boolean {
        var subscriber: Disposable? = null

        try {
            if (filterWheel.position != position
                && position in 1..filterWheel.count
            ) {
                synchronized(filterWheel) {
                    latch.countUp()

                    subscriber = eventBus
                        .filterIsInstance<FilterWheelEvent> { it.device === filterWheel }
                        .subscribe(::onFilterWheelEvent)

                    LOG.info("moving filter wheel ${filterWheel.name} to position $position")

                    filterWheel.moveTo(position)

                    latch.await()
                }
            }
        } finally {
            subscriber?.dispose()
        }

        return true
    }

    override fun closeGracefully() {}

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(FilterWheelMoveTask::class.java)
    }
}
