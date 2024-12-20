package nebulosa.api.wheels

import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.filterwheel.FilterWheelMoveFailed
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.log.w
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.CountUpDownLatch

data class WheelMoveTask(
    @JvmField val job: Job,
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
            LOG.w("failed to move filter wheel. wheel={}, position={}", wheel, position)
            latch.reset()
        }
    }

    override fun run() {
        if (wheel.connected && position in 1..wheel.count && wheel.position != position) {
            LOG.d("Wheel Move started. wheel={}, position={}", wheel, position)

            initialPosition = wheel.position

            latch.countUp()
            wheel.moveTo(position)
            latch.await()

            LOG.d("Wheel Move finished. wheel={}, position={}", wheel, position)
        } else {
            LOG.w("filter wheel not connected or invalid position. position={}, wheel={}", position, wheel)
        }
    }

    override fun onCancel(source: CancellationSource) {
        latch.onCancel(source)
    }

    companion object {

        private val LOG = loggerFor<WheelMoveTask>()
    }
}
