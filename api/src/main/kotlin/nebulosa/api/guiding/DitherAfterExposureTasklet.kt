package nebulosa.api.guiding

import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.StoppableTasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.atomic.AtomicInteger

data class DitherAfterExposureTasklet(val request: DitherAfterExposureRequest) : StoppableTasklet, GuiderListener {

    @Autowired private lateinit var guider: Guider

    private val ditherLatch = CountUpDownLatch()
    private val exposureCount = AtomicInteger()

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        if (request.enabled) {
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

        return RepeatStatus.FINISHED
    }

    override fun stop() {
        ditherLatch.reset(0)
        guider.unregisterGuiderListener(this)
    }

    override fun onDithered(dx: Double, dy: Double) {
        ditherLatch.reset()
    }
}
