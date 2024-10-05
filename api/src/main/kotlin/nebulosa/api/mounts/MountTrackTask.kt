package nebulosa.api.mounts

import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountEvent
import nebulosa.indi.device.mount.MountTrackingChanged
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.util.concurrency.latch.CountUpDownLatch

data class MountTrackTask(
    @JvmField val job: Job,
    @JvmField val mount: Mount,
    @JvmField val enabled: Boolean,
) : Task, MountEventAware {

    private val trackingLatch = CountUpDownLatch()

    override fun handleMountEvent(event: MountEvent) {
        if (event.device === mount && event is MountTrackingChanged && mount.tracking == enabled) {
            trackingLatch.reset()
        }
    }

    override fun run() {
        if (mount.connected && mount.tracking != enabled) {
            trackingLatch.countUp()
            mount.tracking(enabled)
            trackingLatch.await()
        }
    }
}