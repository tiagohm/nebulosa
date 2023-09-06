package nebulosa.api.guiding

import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.GuideCalibrationEntity
import nebulosa.api.data.enums.DitherMode
import nebulosa.api.data.enums.GuideAlgorithmType
import nebulosa.api.data.events.GuideExposureFinished
import nebulosa.api.data.responses.GuidingChartResponse
import nebulosa.api.data.responses.GuidingStarResponse
import nebulosa.api.image.ImageService
import nebulosa.api.image.ImageToken
import nebulosa.api.repositories.GuideCalibrationRepository
import nebulosa.api.services.GuideExposureTask
import nebulosa.api.services.MessageService
import nebulosa.guiding.*
import nebulosa.guiding.internal.*
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.AutoScreenTransformFunction
import nebulosa.imaging.algorithms.SubFrame
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64
import kotlin.math.hypot
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@Service
class GuidingService(
    private val messageService: MessageService,
    private val eventBus: EventBus,
    private val cameraExecutorService: ExecutorService,
    private val guiderExecutorService: ExecutorService,
    private val guideCalibrationRepository: GuideCalibrationRepository,
    private val imageService: ImageService,
) : GuiderListener {

    private val randomDither = RandomDither()
    private val spiralDither = SpiralDither()

    private lateinit var guider: Guider
    private lateinit var camera: Camera
    private lateinit var mount: Mount
    private lateinit var guideOutput: GuideOutput

    private val guideExposureTask = AtomicReference<GuideExposureTask>()
    private val guideImage = AtomicReference<Image>()

    private val xGuideAlgorithms = EnumMap<GuideAlgorithmType, GuideAlgorithm>(GuideAlgorithmType::class.java)
    private val yGuideAlgorithms = EnumMap<GuideAlgorithmType, GuideAlgorithm>(GuideAlgorithmType::class.java)

    init {
        xGuideAlgorithms[GuideAlgorithmType.HYSTERESIS] = HysteresisGuideAlgorithm(GuideAxis.RA_X)
        xGuideAlgorithms[GuideAlgorithmType.LOW_PASS] = LowPassGuideAlgorithm(GuideAxis.RA_X)
        xGuideAlgorithms[GuideAlgorithmType.RESIST_SWITCH] = ResistSwitchGuideAlgorithm(GuideAxis.RA_X)

        yGuideAlgorithms[GuideAlgorithmType.HYSTERESIS] = HysteresisGuideAlgorithm(GuideAxis.DEC_Y)
        yGuideAlgorithms[GuideAlgorithmType.LOW_PASS] = LowPassGuideAlgorithm(GuideAxis.DEC_Y)
        yGuideAlgorithms[GuideAlgorithmType.RESIST_SWITCH] = ResistSwitchGuideAlgorithm(GuideAxis.DEC_Y)
    }

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)

        with(MultiStarGuider()) {
            registerListener(this@GuidingService)
            guider = this
        }
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

    @Synchronized
    fun startLooping(
        camera: Camera, mount: Mount, guideOutput: GuideOutput,
    ) {
        if (guider.isLooping) return
        if (!camera.connected) return

        this.camera = camera
        this.mount = mount
        this.guideOutput = guideOutput

        camera.enableBlob()
        guider.startLooping()
    }

    @Synchronized
    fun stopLooping() {
        if (guider.isLooping) {
            stopGuiding()

            camera.disableBlob()
            camera.abortCapture()
            guider.stopLooping()
        }
    }

    @Synchronized
    fun startGuiding(forceCalibration: Boolean) {
        if (guider.isGuiding) return
        if (!mount.connected || !guideOutput.connected) return

        if (forceCalibration) {
            LOG.info("starting guiding with force calibration")
            guider.clearCalibration()
        } else {
            val calibration = guideCalibrationRepository
                .withCameraAndMountAndGuideOutput(camera.name, mount.name, guideOutput.name)
                ?.toGuideCalibration() ?: GuideCalibration.EMPTY

            if (calibration != GuideCalibration.EMPTY) {
                LOG.info("calibration restored. calibration={}", calibration)
                guider.loadCalibration(calibration)
            } else {
                guider.clearCalibration()
                LOG.info("unable to restore calibration")
                LOG.info("starting guiding with force calibration")
            }
        }

        guider.startGuiding()
    }

    @Synchronized
    fun stopGuiding() {
        if (guider.isGuiding) {
            guider.stopGuiding()
        }
    }

    fun guidingChart(): GuidingChartResponse {
        val chart = guider.stats
        val stats = chart.lastOrNull()
        val rmsTotal = if (stats == null) 0.0 else hypot(stats.rmsRA, stats.rmsDEC)
        return GuidingChartResponse(chart, stats?.rmsRA ?: 0.0, stats?.rmsDEC ?: 0.0, rmsTotal)
    }

    fun guidingStar(): GuidingStarResponse? {
        val image = guideImage.get() ?: return null
        val lockPosition = guider.lockPosition
        val trackBoxSize = searchRegion * 2.0

        return if (lockPosition.valid) {
            val size = min(trackBoxSize, 64.0)

            val centerX = (lockPosition.x - size / 2).toInt()
            val centerY = (lockPosition.y - size / 2).toInt()
            val transformedImage = image.transform(SubFrame(centerX, centerY, size.toInt(), size.toInt()), AutoScreenTransformFunction)

            val fwhm = FWHM(guider.primaryStar)
            val computedFWHM = fwhm.compute(transformedImage)

            val output = Base64OutputStream(128)
            ImageIO.write(transformedImage.transform(fwhm), "PNG", output)

            GuidingStarResponse(
                "data:image/png;base64," + output.base64(),
                guider.lockPosition.x, guider.lockPosition.y,
                guider.primaryStar.x, guider.primaryStar.y,
                guider.primaryStar.peak,
                computedFWHM,
                guider.primaryStar.hfd,
                guider.primaryStar.snr,
            )
        } else {
            null
        }
    }

    fun selectGuideStar(x: Double, y: Double) {
        guider.selectGuideStar(x, y)
    }

    fun deselectGuideStar() {
        guider.deselectGuideStar()
    }

    // TODO: Implement Guide Rate property on mount device.
    // TODO: Pass the rotator angle when implement it.

    private var ditherMode = DitherMode.RANDOM
    private var xGuideAlgorithmType = GuideAlgorithmType.HYSTERESIS
    private var yGuideAlgorithmType = GuideAlgorithmType.HYSTERESIS

    // min: 0.1, max: 10 (px)
    var minimumStarHFD = 1.5

    // min: 0.1, max: 10 (px)
    var maximumStarHFD = 1.5

    override fun capture(duration: Long): Image? {
        return synchronized(guideExposureTask) {
            val task = GuideExposureTask(camera, duration.milliseconds, ImageToken.Guiding)

            guideExposureTask.set(task)
            cameraExecutorService.submit(task).get()
            guideExposureTask.set(null)

            task.firstOrNull()
        }
    }

    override fun guideNorth(duration: Int): Boolean {
        guideOutput.guideNorth(duration)
        LOG.info("guiding north. device={}, duration={} ms", guideOutput.name, duration)
        return true
    }

    override fun guideSouth(duration: Int): Boolean {
        guideOutput.guideSouth(duration)
        LOG.info("guiding south. device={}, duration={} ms", guideOutput.name, duration)
        return true
    }

    override fun guideWest(duration: Int): Boolean {
        guideOutput.guideWest(duration)
        LOG.info("guiding west. device={}, duration={} ms", guideOutput.name, duration)
        return true
    }

    override fun guideEast(duration: Int): Boolean {
        guideOutput.guideEast(duration)
        LOG.info("guiding east. device={}, duration={} ms", guideOutput.name, duration)
        return true
    }

    override fun onLockPositionChanged(position: GuidePoint) {
        sendGuideLockPositionChanged(guider)
    }

    override fun onStarSelected(star: StarPoint) {
        // TODO
    }

    override fun onGuidingDithered(dx: Double, dy: Double) {
        // TODO
    }

    override fun onCalibrationFailed() {
        // TODO
    }

    override fun onGuidingStopped() {
        // TODO
    }

    override fun onLockShiftLimitReached() {
        // TODO
    }

    override fun onLooping(image: Image, number: Int, star: StarPoint?) {
        // TODO
    }

    override fun onStarLost() {
        sendGuideStarLost(guider)
    }

    override fun onLockPositionLost() {
        sendLockPositionLost(guider)
    }

    override fun onStartCalibration() {
        // TODO
    }

    override fun onCalibrationStep(
        calibrationState: CalibrationState,
        direction: GuideDirection,
        stepNumber: Int,
        dx: Double, dy: Double,
        posX: Double, posY: Double,
        distance: Double
    ) {
        // TODO
    }

    override fun onCalibrationCompleted(calibration: GuideCalibration) {
        guideCalibrationRepository.save(GuideCalibrationEntity.from(camera, mount, guideOutput, calibration))
    }

    override fun onGuideStep(stats: GuideStats) {
        // TODO
    }

    fun sendGuideExposureFinished(event: GuideExposureFinished) {
        messageService.sendMessage(GUIDE_EXPOSURE_FINISHED, event)
    }

    fun sendGuideLockPositionChanged(guider: Guider) {
        messageService.sendMessage(GUIDE_LOCK_POSITION_CHANGED, guider)
    }

    fun sendGuideStarLost(guider: Guider) {
        messageService.sendMessage(GUIDE_STAR_LOST, guider)
    }

    fun sendLockPositionLost(guider: Guider) {
        messageService.sendMessage(GUIDE_LOCK_POSITION_LOST, guider)
    }

    // TODO: Move to nebulosa-io and improve it encoding on write (as same Base64InputStream).
    private class Base64OutputStream(size: Int) : ByteArrayOutputStream(size) {

        fun base64() = Base64.encode(buf, 0, count)

        fun base64UrlSafe() = Base64.UrlSafe.encode(buf, 0, count)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<GuidingService>()

        const val GUIDE_EXPOSURE_FINISHED = "GUIDE_EXPOSURE_FINISHED"
        const val GUIDE_LOCK_POSITION_CHANGED = "GUIDE_LOCK_POSITION_CHANGED"
        const val GUIDE_STAR_LOST = "GUIDE_STAR_LOST"
        const val GUIDE_LOCK_POSITION_LOST = "GUIDE_LOCK_POSITION_LOST"
    }
}
