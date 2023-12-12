package nebulosa.api.guiding

import nebulosa.batch.processing.Step
import nebulosa.batch.processing.StepExecution
import nebulosa.batch.processing.StepResult
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.guiding.GuideState
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.atomic.AtomicInteger

data class DitherAfterExposureStep(@JvmField val request: DitherAfterExposureRequest) : Step, GuiderListener {

    @Autowired private lateinit var guider: Guider

    private val ditherLatch = CountUpDownLatch()
    private val exposureCount = AtomicInteger()

    override fun execute(stepExecution: StepExecution): StepResult {
        if (guider.canDither && request.enabled && guider.state == GuideState.GUIDING) {
            if (exposureCount.get() < request.afterExposures) {
                try {
                    guider.registerGuiderListener(this)
                    ditherLatch.countUp()
                    guider.dither(request.amount, request.raOnly)
                    ditherLatch.await()
                } finally {
                    guider.unregisterGuiderListener(this)
                }
            }

            if (exposureCount.incrementAndGet() >= request.afterExposures) {
                exposureCount.set(0)
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
