package nebulosa.api.mounts

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.batch.processing.delay.DelayStep
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountSlewFailed
import nebulosa.indi.device.mount.MountSlewingChanged
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.time.Duration

data class MountSlewStep(
    val mount: Mount,
    val rightAscension: Angle, val declination: Angle,
    val j2000: Boolean = false, val goTo: Boolean = true,
) : Step {

    private val latch = CountUpDownLatch()
    private val settleDelayStep = DelayStep(SETTLE_DURATION)

    @Volatile private var initialRA = mount.rightAscension
    @Volatile private var initialDEC = mount.declination

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountEvent(event: MountEvent) {
        if (event.device === mount) {
            if (event is MountSlewingChanged) {
                if (!mount.slewing && (mount.rightAscension != initialRA || mount.declination != initialDEC)) {
                    latch.reset()
                }
            } else if (event is MountSlewFailed) {
                LOG.warn("failed to slew mount. mount={}", mount)
                latch.reset()
            }
        }
    }

    override fun execute(stepExecution: StepExecution): StepResult {
        if (mount.connected && !mount.parked && !mount.parking && !mount.slewing &&
            rightAscension.isFinite() && declination.isFinite() &&
            (mount.rightAscension != rightAscension || mount.declination != declination)
        ) {
            EventBus.getDefault().register(this)

            latch.countUp()

            LOG.info("moving mount. mount={}, ra={}, dec={}", mount, rightAscension.formatHMS(), declination.formatSignedDMS())

            initialRA = mount.rightAscension
            initialDEC = mount.declination

            if (j2000) {
                if (goTo) mount.goToJ2000(rightAscension, declination)
                else mount.slewToJ2000(rightAscension, declination)
            } else {
                if (goTo) mount.goTo(rightAscension, declination)
                else mount.slewTo(rightAscension, declination)
            }

            latch.await()

            LOG.info("mount moved. mount={}", mount)

            settleDelayStep.execute(stepExecution)

            EventBus.getDefault().unregister(this)
        } else {
            LOG.warn("cannot move mount. mount={}", mount)
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        mount.abortMotion()
        latch.reset()
        settleDelayStep.stop(mayInterruptIfRunning)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<MountSlewStep>()
        @JvmStatic private val SETTLE_DURATION: Duration = Duration.ofSeconds(5)
    }
}
