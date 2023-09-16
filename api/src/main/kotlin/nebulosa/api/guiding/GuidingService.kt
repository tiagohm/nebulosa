package nebulosa.api.guiding

import nebulosa.api.data.responses.GuidingChartResponse
import nebulosa.api.data.responses.GuidingStarResponse
import nebulosa.api.image.ImageService
import nebulosa.api.services.MessageService
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.stereotype.Service
import kotlin.math.hypot

@Service
class GuidingService(
    private val messageService: MessageService,
    private val imageService: ImageService,
    private val guidingExecutor: GuidingExecutor,
) {

    // fun onGuideExposureFinished(event: GuideExposureFinished) {
    // imageService.load(event.task.token, event.image)
    // guideImage.set(event.image)
    // sendGuideExposureFinished(event)
    // }

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
//        val image = guideImage.get() ?: return null
//        val lockPosition = guidingExecutor.lockPosition
//        val trackBoxSize = guidingExecutor.searchRegion * 2.0
//
//        return if (lockPosition.valid) {
//            val size = min(trackBoxSize, 64.0)
//
//            val centerX = (lockPosition.x - size / 2).toInt()
//            val centerY = (lockPosition.y - size / 2).toInt()
//            val transformedImage = image.transform(SubFrame(centerX, centerY, size.toInt(), size.toInt()), AutoScreenTransformFunction)
//
//            val fwhm = FWHM(guidingExecutor.primaryStar)
//            val computedFWHM = fwhm.compute(transformedImage)
//
//            val output = Base64OutputStream(128)
//            ImageIO.write(transformedImage.transform(fwhm), "PNG", output)
//
//            GuidingStarResponse(
//                "data:image/png;base64," + output.base64(),
//                guidingExecutor.lockPosition.x, guidingExecutor.lockPosition.y,
//                guidingExecutor.primaryStar.x, guidingExecutor.primaryStar.y,
//                guidingExecutor.primaryStar.peak,
//                computedFWHM,
//                guidingExecutor.primaryStar.hfd,
//                guidingExecutor.primaryStar.snr,
//            )
//        } else {
//            null
//        }

        return null
    }

    fun selectGuideStar(x: Double, y: Double) {
        guidingExecutor.selectGuideStar(x, y)
    }

    fun deselectGuideStar() {
        guidingExecutor.deselectGuideStar()
    }

    companion object {

        const val GUIDE_EXPOSURE_FINISHED = "GUIDE_EXPOSURE_FINISHED"
    }
}
