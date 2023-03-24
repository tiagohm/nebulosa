package nebulosa.guiding.local

import nebulosa.common.concurrency.PauseableWorker
import java.util.concurrent.atomic.AtomicInteger

data class GuideCameraCapturer(private val guider: Guider) : PauseableWorker("Guide Camera Capture Thread") {

    private val frameNumber = AtomicInteger(1)

    // void MyFrame::OnExposeComplete(usImage *pNewFrame, bool err)
    override fun run() {
        val startTime = System.currentTimeMillis()
        val duration = guider.loopingDuration

        if (guider.pauseType != PauseType.FULL) {
            guider.camera?.also {
                it.capture(duration)
                val frame = Frame(it.image, frameNumber.getAndIncrement())
                guider.updateGuide(frame, false)
            }
        }

        val wait = duration - (System.currentTimeMillis() - startTime)
        if (wait > 0L) Thread.sleep(wait)
    }
}
