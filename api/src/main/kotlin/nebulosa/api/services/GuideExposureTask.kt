package nebulosa.api.services

import nebulosa.api.data.events.GuideExposureFinished
import nebulosa.api.image.ImageToken
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.common.concurrency.ThreadedJob
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.*
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

data class GuideExposureTask(
    val camera: Camera,
    val exposure: Duration,
    val token: ImageToken,
) : ThreadedJob<Image>() {

    val exposureInMicroseconds = exposure.inWholeMicroseconds

    private val latch = CountUpDownLatch()
    private val forceAbort = AtomicBoolean()

    init {
        LOG.info("guide exposure task. camera={} exposure={}", camera, exposure)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onCameraEvent(event: CameraEvent) {
        if (running && event.device === camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    save(event.fits)
                    latch.countDown()
                }
                is CameraExposureAborted,
                is CameraExposureFailed,
                is CameraDetached -> {
                    latch.reset()
                }
            }
        }
    }

    override fun onStart() {
        EventBus.getDefault().register(this)
        camera.enableBlob()
    }

    override fun execute() {
        if (camera.connected && !forceAbort.get()) {
            synchronized(camera) {
                latch.countUp()

                camera.frame(0, 0, camera.maxWidth, camera.maxHeight)
                camera.frameType(FrameType.LIGHT)
                camera.bin(1, 1)
                // camera.gain(gain)
                // camera.offset(offset)
                camera.startCapture(exposureInMicroseconds.microseconds)

                LOG.info("exposuring guiding camera ${camera.name} by $exposure")

                latch.await()

                LOG.info("guiding camera exposure finished. abort={}", forceAbort.get())
            }
        }

        stop()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        camera.disableBlob()
    }

    fun abort() {
        camera.abortCapture()
        forceAbort.set(true)
    }

    private fun save(inputStream: InputStream): Boolean {
        return try {
            val image = Image.openFITS(inputStream).also(::add)
            EventBus.getDefault().post(GuideExposureFinished(this, image))
            true
        } catch (e: Throwable) {
            LOG.error("failed to save FITS", e)
            abort()
            false
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<GuideExposureTask>()
    }
}
