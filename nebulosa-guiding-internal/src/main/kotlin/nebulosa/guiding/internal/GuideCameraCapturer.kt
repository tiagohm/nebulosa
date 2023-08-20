package nebulosa.guiding.internal

import nebulosa.common.concurrency.ThreadedJob
import nebulosa.guiding.NoiseReductionMethod
import nebulosa.imaging.algorithms.Mean
import nebulosa.log.loggerFor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

internal data class GuideCameraCapturer(private val guider: MultiStarGuider) : ThreadedJob<Unit>() {

    private val frameNumber = AtomicInteger(1)

    override fun execute() {
        val startedAt = System.currentTimeMillis()
        val duration = guider.device.cameraExposureTime

        if (guider.pauseType != PauseType.FULL) {
            sleepWhile { guider.device.mountIsBusy }

            LOG.info("starting frame capture. exposure={} ms", duration)
            var frame = guider.device.capture(duration) ?: return
            LOG.info("frame capture finished")

            if (guider.device.noiseReductionMethod == NoiseReductionMethod.MEAN) {
                frame = frame.transform(Mean)
            }

            if (!guider.device.mountIsBusy) {
                guider.updateGuide(frame, frameNumber.getAndIncrement(), false)
            }

            if (guider.device.cameraExposureDelay > 0L) {
                Thread.sleep(guider.device.cameraExposureDelay)
            }
        }

        val elapsedTime = System.currentTimeMillis() - startedAt
        Thread.sleep(max(0L, duration - elapsedTime))
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<GuideCameraCapturer>()
    }
}
