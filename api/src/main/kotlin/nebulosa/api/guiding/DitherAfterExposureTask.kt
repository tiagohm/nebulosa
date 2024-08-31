package nebulosa.api.guiding

import nebulosa.api.tasks.AbstractTask
import nebulosa.guiding.GuideState
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import nebulosa.log.loggerFor
import nebulosa.util.concurrency.cancellation.CancellationListener
import nebulosa.util.concurrency.cancellation.CancellationSource
import nebulosa.util.concurrency.cancellation.CancellationToken
import nebulosa.util.concurrency.latch.CountUpDownLatch
import java.time.Duration
import kotlin.system.measureTimeMillis

data class DitherAfterExposureTask(
    @JvmField val guider: Guider?,
    @JvmField val request: DitherAfterExposureRequest,
) : AbstractTask<DitherAfterExposureEvent>(), GuiderListener, CancellationListener {

    private val ditherLatch = CountUpDownLatch()

    @Volatile private var dx = 0.0
    @Volatile private var dy = 0.0
    @Volatile private var elapsedTime = Duration.ZERO

    override fun execute(cancellationToken: CancellationToken) {
        if (guider != null && guider.canDither && request.enabled
            && guider.state == GuideState.GUIDING
            && !cancellationToken.isCancelled
        ) {
            LOG.info("Dither started. request={}", request)

            try {
                cancellationToken.listen(this)
                guider.registerGuiderListener(this)
                ditherLatch.countUp()

                sendEvent(DitherAfterExposureState.STARTED)

                elapsedTime = Duration.ofMillis(measureTimeMillis {
                    guider.dither(request.amount, request.raOnly)
                    ditherLatch.await()
                })
            } finally {
                sendEvent(DitherAfterExposureState.FINISHED)

                guider.unregisterGuiderListener(this)
                cancellationToken.unlisten(this)

                LOG.info("Dither finished. elapsedTime={}, request={}", elapsedTime, request)
            }
        }
    }

    override fun onDithered(dx: Double, dy: Double) {
        this.dx = dx
        this.dy = dy

        sendEvent(DitherAfterExposureState.DITHERED)
        LOG.info("dithered. dx={}, dy={}", dx, dy)
        ditherLatch.reset()
    }

    override fun onCancel(source: CancellationSource) {
        ditherLatch.onCancel(source)
    }

    override fun reset() {
        dx = 0.0
        dy = 0.0
        elapsedTime = Duration.ZERO
    }

    private fun sendEvent(state: DitherAfterExposureState) {
        onNext(DitherAfterExposureEvent(this, state, dx, dy, elapsedTime))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DitherAfterExposureTask>()
    }
}
