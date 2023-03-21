package nebulosa.guiding.local

import nebulosa.common.concurrency.PauseableWorker
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class GuideCameraCapturer(
    private val guider: Guider,
    private val camera: AtomicReference<GuideCamera>,
) : PauseableWorker("Guide Camera Capture Thread") {

    private val duration = AtomicLong(1000L) // ms

    @Synchronized
    fun start(duration: Long) {
        this.duration.set(duration)
        start()
    }

    override fun run() {
        val startTime = System.currentTimeMillis()

        camera.get()?.capture(duration.get())
        val image = camera.get()?.image
        if (image != null) guider.updateGuide(image, false)

        val sleepDuration = duration.get() - (System.currentTimeMillis() - startTime)
        if (sleepDuration > 0L) Thread.sleep(sleepDuration)
    }
}
