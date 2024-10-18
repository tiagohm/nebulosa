package nebulosa.api.rotators

import nebulosa.indi.device.rotator.*
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.latch.CountUpDownLatch

data class RotatorMoveTask(
    @JvmField val job: Job,
    @JvmField val rotator: Rotator,
    @JvmField val angle: Double,
) : Task, RotatorEventAware {

    private val latch = CountUpDownLatch()

    @Volatile private var moving = false

    override fun handleRotatorEvent(event: RotatorEvent) {
        when (event) {
            is RotatorMovingChanged -> if (event.device.moving) moving = true else if (moving) latch.reset()
            is RotatorAngleChanged -> if (moving && !event.device.moving) latch.reset()
            is RotatorMoveFailed -> latch.reset()
        }
    }

    override fun run() {
        if (!job.isCancelled && rotator.connected && !rotator.moving &&
            angle != rotator.angle && angle in 0.0..rotator.maxAngle
        ) {
            LOG.d("Rotator move started. rotator={}", rotator)
            latch.countUp()
            moving = true
            rotator.moveRotator(angle)
            latch.await()
            moving = false
            LOG.d("Rotator move finished. rotator={}", rotator)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<RotatorMoveTask>()
    }
}