package nebulosa.api.mounts

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.tasks.AbstractTask
import nebulosa.api.tasks.delay.DelayEvent
import nebulosa.api.tasks.delay.DelayTask
import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.mount.Mount
import nebulosa.log.loggerFor

data class MountMoveTask(
    @JvmField val mount: Mount,
    @JvmField val request: MountMoveRequest,
) : AbstractTask<MountMoveEvent>(), CancellationListener, Consumer<DelayEvent> {

    private val delayTask = DelayTask(request.duration)

    init {
        delayTask.subscribe(this)
    }

    override fun execute(cancellationToken: CancellationToken) {
        if (!cancellationToken.isCancelled && request.duration.toMillis() > 0) {
            mount.slewRates.takeIf { !request.speed.isNullOrBlank() }
                ?.find { it.name == request.speed }
                ?.also { mount.slewRate(it) }

            mount.move(request.direction, true)

            LOG.info("Mount Move started. mount={}, request={}", mount, request)

            try {
                cancellationToken.listen(this)
                delayTask.execute(cancellationToken)
            } finally {
                stop()
                cancellationToken.unlisten(this)
            }

            LOG.info("Mount Move finished. mount={}, request={}", mount, request)
        }
    }

    override fun onCancel(source: CancellationSource) {
        stop()
    }

    fun stop() {
        mount.move(request.direction, false)
    }

    override fun accept(event: DelayEvent) {
        onNext(MountMoveEvent(this, event.remainingTime, event.progress))
    }

    override fun close() {
        delayTask.close()
        super.close()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<MountMoveTask>()

        @JvmStatic
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
