package nebulosa.api.guiding

import nebulosa.guiding.GuideState
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import nebulosa.job.manager.Job
import nebulosa.job.manager.Task
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.util.Resettable
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.latch.CountUpDownLatch

data class DitherAfterExposureTask(
    @JvmField val job: Job,
    @JvmField val guider: Guider?,
    @JvmField val request: DitherAfterExposureRequest,
) : Task, GuiderListener, Resettable {

    private val ditherLatch = CountUpDownLatch()

    override fun run() {
        if (guider != null && guider.canDither && request.enabled
            && guider.state == GuideState.GUIDING
            && !job.isCancelled
        ) {
            LOG.debug { "Dither started. request=$request" }

            guider.registerGuiderListener(this)
            ditherLatch.countUp()
            guider.dither(request.amount, request.raOnly)
            ditherLatch.await()
            guider.unregisterGuiderListener(this)

            LOG.debug { "Dither finished. request=$request" }
        }
    }

    override fun onDithered(dx: Double, dy: Double) {
        job.accept(DitherAfterExposureDithered(job, this, dx, dy))
        LOG.debug { "dithered. dx=$dx, dy=$dy" }
        ditherLatch.reset()
    }

    override fun onCancel(source: CancellationSource) {
        ditherLatch.onCancel(source)
    }

    override fun reset() {
        ditherLatch.reset()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DitherAfterExposureTask>()
    }
}
