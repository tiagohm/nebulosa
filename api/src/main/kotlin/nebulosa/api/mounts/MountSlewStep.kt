package nebulosa.api.mounts

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountSlewFailed
import nebulosa.indi.device.mount.MountSlewingChanged
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

data class MountSlewStep(
    val mount: Mount,
    val rightAscension: Angle, val declination: Angle,
    val j2000: Boolean = false, val goTo: Boolean = true,
) : Step {

    private val latch = CountUpDownLatch()

    private val initialRA = mount.rightAscension
    private val initialDEC = mount.declination

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFilterWheelEvent(event: MountEvent) {
        if (event is MountSlewingChanged) {
            if (!mount.slewing && mount.rightAscension != initialRA && mount.declination != initialDEC) {
                latch.reset()
            }
        } else if (event is MountSlewFailed) {
            LOG.warn("failed to slew mount. mount={}", mount)
            latch.reset()
        }
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (mount.connected && rightAscension.isFinite() &&
            declination.isFinite() && mount.rightAscension != initialRA
            && mount.declination != initialDEC
        ) {
            EventBus.getDefault().register(this)

            latch.countUp()

            if (j2000) {
                if (goTo) mount.goToJ2000(rightAscension, declination)
                else mount.slewToJ2000(rightAscension, declination)
            } else {
                if (goTo) mount.goTo(rightAscension, declination)
                else mount.slewTo(rightAscension, declination)
            }

            latch.await()

            EventBus.getDefault().unregister(this)
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        latch.reset()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<MountSlewStep>()
    }
}
