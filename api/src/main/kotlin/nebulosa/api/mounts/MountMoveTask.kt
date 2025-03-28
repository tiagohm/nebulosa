package nebulosa.api.mounts

import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.mount.Mount
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.job.manager.delay.DelayTask
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.util.Startable
import nebulosa.util.Stoppable
import nebulosa.util.concurrency.cancellation.CancellationSource

data class MountMoveTask(
    @JvmField val job: Job,
    @JvmField val mount: Mount,
    @JvmField val request: MountMoveRequest,
) : Task, Startable, Stoppable {

    @JvmField val delayTask = DelayTask(job, request.duration)

    override fun run() {
        if (!job.isCancelled && delayTask.duration > 0) {
            LOG.d { debug("Mount Move started. mount={}, request={}", mount, request) }

            mount.slewRates.takeIf { !request.speed.isNullOrBlank() }
                ?.find { it.value == request.speed }
                ?.also { mount.slewRate(it) }

            start()
            delayTask.run()
            stop()

            LOG.d { debug("Mount Move finished. mount={}, request={}", mount, request) }
        }
    }

    override fun onCancel(source: CancellationSource) {
        delayTask.onCancel(source)
        stop()
    }

    override fun start() {
        mount.move(request.direction, true)
    }

    override fun stop() {
        mount.move(request.direction, false)
    }

    companion object {

        private val LOG = loggerFor<MountMoveTask>()

        private fun Mount.move(direction: GuideDirection, enabled: Boolean) {
            when (direction) {
                GuideDirection.NORTH -> moveNorth(enabled)
                GuideDirection.SOUTH -> moveSouth(enabled)
                GuideDirection.WEST -> moveWest(enabled)
                GuideDirection.EAST -> moveEast(enabled)
            }
        }
    }
}
