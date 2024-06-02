package nebulosa.api.guiding

import nebulosa.api.tasks.AbstractTask
import nebulosa.common.concurrency.cancel.CancellationListener
import nebulosa.common.concurrency.cancel.CancellationSource
import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.guiding.GuideState
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import nebulosa.log.loggerFor
import java.time.Duration
import kotlin.system.measureTimeMillis

data class DitherAfterExposureTask(
    @JvmField val guider: Guider?,
    @JvmField val request: DitherAfterExposureRequest,
) : AbstractTask<DitherAfterExposureEvent>(), GuiderListener, CancellationListener {

    private val ditherLatch = CountUpDownLatch()

    @Volatile private var state = DitherAfterExposureState.IDLE
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

                state = DitherAfterExposureState.STARTED
                sendEvent()

                elapsedTime = Duration.ofMillis(measureTimeMillis {
                    guider.dither(request.amount, request.raOnly)
                    ditherLatch.await()
                })
            } finally {
                state = DitherAfterExposureState.FINISHED
                sendEvent()

                guider.unregisterGuiderListener(this)
                cancellationToken.unlisten(this)

                LOG.info("Dither finished. request={}", request)
            }
        }
    }

    override fun onDithered(dx: Double, dy: Double) {
        this.dx = dx
        this.dy = dy
        state = DitherAfterExposureState.DITHERED

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
        state = DitherAfterExposureState.IDLE
    }

    private fun sendEvent() {
        onNext(DitherAfterExposureEvent(this, state, dx, dy, elapsedTime))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DitherAfterExposureTask>()
    }
}
