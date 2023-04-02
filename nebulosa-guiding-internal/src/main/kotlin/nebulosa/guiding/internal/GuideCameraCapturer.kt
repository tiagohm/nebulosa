package nebulosa.guiding.internal

import nebulosa.common.concurrency.Worker
import nebulosa.guiding.NoiseReductionMethod
import nebulosa.imaging.algorithms.Mean
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

internal data class GuideCameraCapturer(private val guider: MultiStarGuider) : Worker() {

    private val frameNumber = AtomicInteger(1)

    override fun execute() {
        val startTime = System.currentTimeMillis()
        val duration = guider.device.cameraExposure

        if (guider.pauseType != PauseType.FULL) {
            while (guider.device.mountIsSlewing) Thread.sleep(10L)
            LOG.info("starting frame capture. exposure={} ms", duration)
            guider.device.capture(duration)
            var frame = guider.device.cameraImage
            LOG.info("frame capture finished")

            if (guider.noiseReductionMethod == NoiseReductionMethod.MEAN) {
                frame = frame.transform(MEAN_FILTER)
            }

            if (!guider.device.mountIsSlewing) {
                guider.updateGuide(frame, frameNumber.getAndIncrement(), false)
            }
        }

        val delta = System.currentTimeMillis() - startTime
        val wait = max(0L, duration - delta)

        Thread.sleep(wait)
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuideCameraCapturer::class.java)
        @JvmStatic private val MEAN_FILTER = Mean()
    }
}
