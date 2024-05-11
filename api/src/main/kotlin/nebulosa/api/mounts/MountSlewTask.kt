package nebulosa.api.mounts

import nebulosa.api.tasks.Task
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountSlewFailed
import nebulosa.indi.device.mount.MountSlewingChanged
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import java.time.Duration

data class MountSlewTask(
    @JvmField val mount: Mount,
    @JvmField val rightAscension: Angle, @JvmField val declination: Angle,
    @JvmField val j2000: Boolean = false, @JvmField val goTo: Boolean = true,
) : Task<Unit>(), CancellationListener {

    private val delayTask = DelayTask(SETTLE_DURATION)
    private val latch = CountUpDownLatch()

    @Volatile private var initialRA = mount.rightAscension
    @Volatile private var initialDEC = mount.declination

    fun handleMountEvent(event: MountEvent) {
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

    override fun execute(cancellationToken: CancellationToken) {
        if (!cancellationToken.isDone &&
            mount.connected && !mount.parked && !mount.parking && !mount.slewing &&
            rightAscension.isFinite() && declination.isFinite() &&
            (mount.rightAscension != rightAscension || mount.declination != declination)
        ) {
            latch.countUp()

            LOG.info("Mount Slew started. mount={}, ra={}, dec={}", mount, rightAscension.formatHMS(), declination.formatSignedDMS())

            initialRA = mount.rightAscension
            initialDEC = mount.declination

            try {
                cancellationToken.listen(this)

                if (j2000) {
                    if (goTo) mount.goToJ2000(rightAscension, declination)
                    else mount.slewToJ2000(rightAscension, declination)
                } else {
                    if (goTo) mount.goTo(rightAscension, declination)
                    else mount.slewTo(rightAscension, declination)
                }

                latch.await()
            } finally {
                cancellationToken.unlisten(this)
            }

            LOG.info("Mount Slew finished. mount={}, ra={}, dec={}", mount, rightAscension.formatHMS(), declination.formatSignedDMS())

            delayTask.execute(cancellationToken)
        } else {
            LOG.warn("cannot slew mount. mount={}, ra={}, dec={}", mount, rightAscension.formatHMS(), declination.formatSignedDMS())
        }
    }

    fun stop() {
        mount.abortMotion()
        latch.reset()
    }

    override fun onCancelled(source: CancellationSource) {
        stop()
    }

    override fun close() {
        delayTask.close()
        super.close()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<MountSlewTask>()
        @JvmStatic private val SETTLE_DURATION: Duration = Duration.ofSeconds(5)
    }
}
