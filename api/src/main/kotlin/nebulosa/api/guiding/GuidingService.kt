package nebulosa.api.guiding

import jakarta.annotation.PostConstruct
import nebulosa.api.data.events.GuideExposureFinished
import nebulosa.api.data.responses.GuidingChartResponse
import nebulosa.api.data.responses.GuidingStarResponse
import nebulosa.api.image.ImageService
import nebulosa.api.services.MessageService
import nebulosa.guiding.*
import nebulosa.guiding.internal.*
import nebulosa.imaging.algorithms.AutoScreenTransformFunction
import nebulosa.imaging.algorithms.SubFrame
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.io.Base64OutputStream
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.hypot
import kotlin.math.min

@Service
class GuidingService(
    private val messageService: MessageService,
    private val eventBus: EventBus,
    private val imageService: ImageService,
    private val guidingExecutor: GuidingExecutor,
) : GuiderListener {

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onGuideExposureFinished(event: GuideExposureFinished) {
        imageService.load(event.task.token, event.image)
        guideImage.set(event.image)
        sendGuideExposureFinished(event)
    }

    fun connect(guideOutput: GuideOutput) {
        guideOutput.connect()
    }

    fun disconnect(guideOutput: GuideOutput) {
        guideOutput.disconnect()
    }

    fun startLooping(
        camera: Camera, mount: Mount, guideOutput: GuideOutput,
        guideStartLooping: GuideStartLoopingRequest,
    ) {
        guidingExecutor.startLooping(camera, mount, guideOutput, guideStartLooping)
    }

    fun stop() {
        guidingExecutor.stop()
    }

    fun startGuiding(forceCalibration: Boolean) {
        guidingExecutor.startGuiding(forceCalibration)
    }

    fun guidingChart(): GuidingChartResponse {
        val chart = guidingExecutor.stats
        val stats = chart.lastOrNull()
        val rmsTotal = if (stats == null) 0.0 else hypot(stats.rmsRA, stats.rmsDEC)
        return GuidingChartResponse(chart, stats?.rmsRA ?: 0.0, stats?.rmsDEC ?: 0.0, rmsTotal)
    }

    fun guidingStar(): GuidingStarResponse? {
        val image = guideImage.get() ?: return null
        val lockPosition = guidingExecutor.lockPosition
        val trackBoxSize = guidingExecutor.searchRegion * 2.0

        return if (lockPosition.valid) {
            val size = min(trackBoxSize, 64.0)

            val centerX = (lockPosition.x - size / 2).toInt()
            val centerY = (lockPosition.y - size / 2).toInt()
            val transformedImage = image.transform(SubFrame(centerX, centerY, size.toInt(), size.toInt()), AutoScreenTransformFunction)

            val fwhm = FWHM(guidingExecutor.primaryStar)
            val computedFWHM = fwhm.compute(transformedImage)

            val output = Base64OutputStream(128)
            ImageIO.write(transformedImage.transform(fwhm), "PNG", output)

            GuidingStarResponse(
                "data:image/png;base64," + output.base64(),
                guidingExecutor.lockPosition.x, guidingExecutor.lockPosition.y,
                guidingExecutor.primaryStar.x, guidingExecutor.primaryStar.y,
                guidingExecutor.primaryStar.peak,
                computedFWHM,
                guidingExecutor.primaryStar.hfd,
                guidingExecutor.primaryStar.snr,
            )
        } else {
            null
        }
    }

    fun selectGuideStar(x: Double, y: Double) {
        guidingExecutor.selectGuideStar(x, y)
    }

    fun deselectGuideStar() {
        guidingExecutor.deselectGuideStar()
    }

    fun sendGuideExposureFinished(event: GuideExposureFinished) {
        messageService.sendMessage(GUIDE_EXPOSURE_FINISHED, event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<GuidingService>()

        const val GUIDE_EXPOSURE_FINISHED = "GUIDE_EXPOSURE_FINISHED"
        const val GUIDE_LOCK_POSITION_CHANGED = "GUIDE_LOCK_POSITION_CHANGED"
        const val GUIDE_STAR_LOST = "GUIDE_STAR_LOST"
        const val GUIDE_LOCK_POSITION_LOST = "GUIDE_LOCK_POSITION_LOST"
    }
}
