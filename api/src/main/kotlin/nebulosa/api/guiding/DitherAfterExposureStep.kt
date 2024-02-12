package nebulosa.api.guiding

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.guiding.GuideState
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener

data class DitherAfterExposureStep(
    @JvmField val request: DitherAfterExposureRequest,
    @JvmField val guider: Guider,
) : Step, GuiderListener {

    private val ditherLatch = CountUpDownLatch()
    @Volatile private var exposureCount = 0

    override fun execute(stepExecution: StepExecution): StepResult {
        if (guider.canDither && request.enabled && guider.state == GuideState.GUIDING) {
            if (exposureCount < request.afterExposures) {
                try {
                    guider.registerGuiderListener(this)
                    ditherLatch.countUp()
                    guider.dither(request.amount, request.raOnly)
                    ditherLatch.await()
                } finally {
                    guider.unregisterGuiderListener(this)
                }
            }

            if (++exposureCount >= request.afterExposures) {
                exposureCount = 0
            }
        }

        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        ditherLatch.reset()
    }

    override fun onDithered(dx: Double, dy: Double) {
        ditherLatch.reset()
    }
}
