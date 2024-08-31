package nebulosa.api.wheels

import nebulosa.api.tasks.Task
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.filterwheel.FilterWheelMoveFailed
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationToken
import nebulosa.util.concurrency.latch.CountUpDownLatch

data class WheelMoveTask(
    @JvmField val wheel: FilterWheel,
    @JvmField val position: Int,
) : Task, WheelEventAware {

    private val latch = CountUpDownLatch()

    @Volatile private var initialPosition = wheel.position

    override fun handleFilterWheelEvent(event: FilterWheelEvent) {
        if (event is FilterWheelPositionChanged) {
            if (initialPosition != wheel.position && wheel.position == position) {
                latch.reset()
            }
        } else if (event is FilterWheelMoveFailed) {
            LOG.warn("failed to move filter wheel. wheel={}, position={}", wheel, position)
            latch.reset()
        }
    }

    override fun execute(cancellationToken: CancellationToken) {
        if (wheel.connected && position in 1..wheel.count && wheel.position != position) {
            initialPosition = wheel.position

            LOG.info("Filter Wheel Move started. wheel={}, position={}", wheel, position)

            try {
                cancellationToken.listen(latch)
                latch.countUp()
                wheel.moveTo(position)
                latch.await()
            } finally {
                cancellationToken.unlisten(latch)
                LOG.info("Filter Wheel Move finished. wheel={}, position={}", wheel, position)
            }
        } else {
            LOG.warn("filter wheel not connected or invalid position. position={}, wheel={}", position, wheel)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<WheelMoveTask>()
    }
}
