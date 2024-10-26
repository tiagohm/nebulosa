package nebulosa.api.mounts

import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountSlewFailed
import nebulosa.indi.device.mount.MountSlewingChanged
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.job.manager.delay.DelayTask
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.log.w
import nebulosa.math.Angle
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import nebulosa.util.Resettable
import nebulosa.util.Stoppable
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.CountUpDownLatch
import java.time.Duration

data class MountSlewTask(
    @JvmField val job: Job,
    @JvmField val mount: Mount,
    @JvmField val rightAscension: Angle, @JvmField val declination: Angle,
    @JvmField val j2000: Boolean = false, @JvmField val goTo: Boolean = true,
) : Task, Stoppable, Resettable, MountEventAware {

    private val delayTask = DelayTask(job, SETTLE_DURATION)
    private val latch = CountUpDownLatch()

    @Volatile private var initialRA = mount.rightAscension
    @Volatile private var initialDEC = mount.declination

    override fun handleMountEvent(event: MountEvent) {
        if (event.device === mount) {
            if (event is MountSlewingChanged) {
                if (!mount.slewing && (mount.rightAscension != initialRA || mount.declination != initialDEC)) {
                    latch.reset()
                }
            } else if (event is MountSlewFailed) {
                LOG.w("failed to slew mount. mount={}", mount)
                latch.reset()
            }
        }
    }

    override fun run() {
        if (!job.isCancelled &&
            mount.connected && !mount.parked && !mount.parking && !mount.slewing &&
            rightAscension.isFinite() && declination.isFinite() &&
            (mount.rightAscension != rightAscension || mount.declination != declination)
        ) {
            LOG.d("Mount Slew started. mount={}, ra={}, dec={}", mount, rightAscension.formatHMS(), declination.formatSignedDMS())

            latch.countUp()

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
            LOG.d("Mount Slew finished. mount={}, ra={}, dec={}", mount, rightAscension.formatHMS(), declination.formatSignedDMS())
            delayTask.run()
        } else {
            LOG.w("cannot slew mount. mount={}, ra={}, dec={}", mount, rightAscension.formatHMS(), declination.formatSignedDMS())
        }
    }

    override fun stop() {
        mount.abortMotion()
        latch.reset()
    }

    override fun reset() {
        latch.reset()
    }

    override fun onCancel(source: CancellationSource) {
        stop()
    }

    companion object {

        private val LOG = loggerFor<MountSlewTask>()
        private val SETTLE_DURATION: Duration = Duration.ofSeconds(5)
    }
}
