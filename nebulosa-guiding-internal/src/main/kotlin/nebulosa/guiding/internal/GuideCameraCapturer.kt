package nebulosa.guiding.internal

import nebulosa.common.concurrency.PauseableWorker
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

internal data class GuideCameraCapturer(private val guider: MultiStarGuider) : PauseableWorker("Guide Camera Capture Thread") {

    private val frameNumber = AtomicInteger(1)

    override fun run() {
        val startTime = System.currentTimeMillis()
        val duration = guider.device.cameraExposure

        if (guider.pauseType != PauseType.FULL) {
            LOG.info("starting frame capture. exposure={} ms", duration)
            guider.device.capture(duration)
            val frame = Frame(guider.device.cameraImage, frameNumber.getAndIncrement())
            LOG.info("frame capture finished.")
            guider.updateGuide(frame, false)
        }

        val delta = System.currentTimeMillis() - startTime
        val wait = max(0L, duration - delta)

        Thread.sleep(wait)
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuideCameraCapturer::class.java)
    }
}
