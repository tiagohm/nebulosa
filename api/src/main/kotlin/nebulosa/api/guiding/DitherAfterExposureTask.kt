package nebulosa.api.guiding

import nebulosa.api.tasks.Task
import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.common.time.Stopwatch
import nebulosa.guiding.GuideState
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import nebulosa.log.loggerFor

data class DitherAfterExposureTask(
    @JvmField val guider: Guider?,
    @JvmField val request: DitherAfterExposureRequest,
) : Task<DitherAfterExposureEvent>(), GuiderListener, CancellationListener {

    private val ditherLatch = CountUpDownLatch()
    private val stopwatch = Stopwatch()

    override fun execute(cancellationToken: CancellationToken) {
        if (guider != null && guider.canDither && request.enabled
            && guider.state == GuideState.GUIDING
            && !cancellationToken.isDone
        ) {
            LOG.info("dithering. request={}", request)

            try {
                cancellationToken.listen(this)
                guider.registerGuiderListener(this)
                ditherLatch.countUp()
                onNext(DitherAfterExposureEvent.Started(this))
                stopwatch.start()
                guider.dither(request.amount, request.raOnly)
                ditherLatch.await()
                stopwatch.stop()
            } finally {
                onNext(DitherAfterExposureEvent.Finished(this, stopwatch.elapsed))
                guider.unregisterGuiderListener(this)
                cancellationToken.unlisten(this)
                stopwatch.reset()
            }
        }
    }

    override fun onDithered(dx: Double, dy: Double) {
        onNext(DitherAfterExposureEvent.Dithered(this, dx, dy))
        ditherLatch.reset()
    }

    override fun onCancelled(source: CancellationSource) {
        ditherLatch.onCancelled(source)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DitherAfterExposureTask>()
    }
}
