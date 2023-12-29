package nebulosa.api.wheels

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import nebulosa.indi.device.filterwheel.FilterWheelMoveFailed
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

data class WheelStep(
    val wheel: FilterWheel,
    val position: Int,
) : Step {

    private val latch = CountUpDownLatch()
    @Volatile private var initialPosition = wheel.position

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFilterWheelEvent(event: FilterWheelEvent) {
        if (event is FilterWheelPositionChanged) {
            if (initialPosition != wheel.position && wheel.position == position) {
                latch.reset()
            }
        } else if (event is FilterWheelMoveFailed) {
            LOG.warn("failed to move filter wheel. wheel={}, position={}", wheel, position)
            latch.reset()
        }
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (wheel.connected && position > 0 && wheel.position != position) {
            initialPosition = wheel.position

            EventBus.getDefault().register(this)

            latch.countUp()
            wheel.moveTo(position)
            latch.await()

            EventBus.getDefault().unregister(this)
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        latch.reset()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<WheelStep>()
    }
}
