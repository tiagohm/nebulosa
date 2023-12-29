package nebulosa.api.focusers

import nebulosa.api.wheels.WheelStep
import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.indi.device.focuser.FocuserMoveFailed
import nebulosa.indi.device.focuser.FocuserPositionChanged
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs

class FocusOffsetStep(
    val focuser: Focuser,
    val offset: Int,
) : Step {

    private val latch = CountUpDownLatch()
    private val expectedPosition = IntArray(2)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFocuserEvent(event: FocuserEvent) {
        if (event is FocuserPositionChanged) {
            if (focuser.position == expectedPosition[0] || focuser.position == expectedPosition[1]) {
                latch.reset()
            }
        } else if (event is FocuserMoveFailed) {
            LOG.warn("failed to move focuser. focuser={}, offset={}", focuser, offset)
            latch.reset()
        }
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (focuser.connected && (focuser.canRelativeMove || focuser.canAbsoluteMove) && offset != 0) {

            EventBus.getDefault().register(this)

            latch.countUp()

            expectedPosition[0] = focuser.position + abs(offset)
            expectedPosition[1] = focuser.position - abs(offset)

            if (focuser.canAbsoluteMove) focuser.moveFocusTo(focuser.position + offset)
            else if (offset > 0) focuser.moveFocusOut(offset)
            else focuser.moveFocusIn(-offset)

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
