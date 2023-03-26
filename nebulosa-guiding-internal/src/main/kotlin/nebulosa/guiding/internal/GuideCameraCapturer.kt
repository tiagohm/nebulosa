package nebulosa.guiding.internal

import nebulosa.common.concurrency.PauseableWorker
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

data class GuideCameraCapturer(private val guider: MultiStarGuider) : PauseableWorker("Guide Camera Capture Thread") {

    private val frameNumber = AtomicInteger(1)

    // void MyFrame::OnExposeComplete(usImage *pNewFrame, bool err)
    override fun run() {
        val startTime = System.currentTimeMillis()
        val duration = guider.camera.exposure

        if (guider.pauseType != PauseType.FULL) {
            guider.camera?.also {
                LOG.info("starting frame capture. exposure={} ms", duration)
                it.capture(duration)
                val frame = Frame(it.image, frameNumber.getAndIncrement())
                LOG.info("frame capture finished.")
                guider.updateGuide(frame, false)
            }
        }

        val delta = System.currentTimeMillis() - startTime
        val wait = max(0L, duration - delta)

        LOG.info("frame capture took {} ms. exposure={} ms, wait={} ms", delta, duration, wait)

        Thread.sleep(wait)
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuideCameraCapturer::class.java)
    }
}
