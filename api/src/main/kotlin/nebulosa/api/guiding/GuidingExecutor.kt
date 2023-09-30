package nebulosa.api.guiding

import jakarta.annotation.PostConstruct
import nebulosa.api.services.MessageService
import nebulosa.guiding.*
import nebulosa.guiding.internal.*
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.PierSide
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.support.ReferenceJobFactory
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

// TODO: Implement Guide Rate property on mount device.

@Component
class GuidingExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val asyncJobLauncher: JobLauncher,
    private val platformTransactionManager: PlatformTransactionManager,
    private val jobRegistry: JobRegistry,
    private val guideCalibrationRepository: GuideCalibrationRepository,
    private val messageService: MessageService,
) : GuidingCamera, GuidingMount, GuidingRotator, GuidingPulse, GuiderListener {

    private val guider = MultiStarGuider()
    private val guideCamera = AtomicReference<Camera>()
    private val guideMount = AtomicReference<Mount>()
    private val guideOutput = AtomicReference<GuideOutput>()
    private val jobExecutionCounter = AtomicInteger(1)
    private val stepExecutionCounter = AtomicInteger(1)
    private val runningJob = AtomicReference<Pair<Job, JobExecution>>()
    private val randomDither = RandomDither()
    private val spiralDither = SpiralDither()

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
        guider.camera = this
        guider.mount = this
        guider.pulse = this
        guider.registerListener(this)
    }

    val stats
        get() = guider.stats

    val lockPosition
        get() = guider.lockPosition

    val primaryStar
        get() = guider.primaryStar

    override val binning
        get() = guideCamera.get()?.binX ?: 1

    override val pixelScale
        get() = guideCamera.get()?.pixelSizeX ?: 1.0

    override val exposureTime: Long
        get() = TODO("Not yet implemented")

    override val isBusy: Boolean
        get() = TODO("Not yet implemented")

    override val rightAscension
        get() = guideMount.get()?.rightAscension ?: 0.0

    override val declination
        get() = guideMount.get()?.declination ?: 0.0

    override val rightAscensionGuideRate: Double
        get() = TODO("Not yet implemented")

    override val declinationGuideRate: Double
        get() = TODO("Not yet implemented")

    override val isPierSideAtEast
        get() = guideMount.get()?.pierSide == PierSide.EAST

    override fun guideNorth(duration: Int): Boolean {
        val guideOutput = guideOutput.get() ?: return false
        guideOutput.guideNorth(duration)
        LOG.info("guiding north. device={}, duration={} ms", guideOutput.name, duration)
        return true
    }

    override fun guideSouth(duration: Int): Boolean {
        val guideOutput = guideOutput.get() ?: return false
        guideOutput.guideSouth(duration)
        LOG.info("guiding south. device={}, duration={} ms", guideOutput.name, duration)
        return true
    }

    override fun guideWest(duration: Int): Boolean {
        val guideOutput = guideOutput.get() ?: return false
        guideOutput.guideWest(duration)
        LOG.info("guiding west. device={}, duration={} ms", guideOutput.name, duration)
        return true
    }

    override fun guideEast(duration: Int): Boolean {
        val guideOutput = guideOutput.get() ?: return false
        guideOutput.guideEast(duration)
        LOG.info("guiding east. device={}, duration={} ms", guideOutput.name, duration)
        return true
    }

    // TODO: Ajustar quando implementar o Rotator.
    override val angle = 0.0

    @Synchronized
    fun startLooping(
        camera: Camera, mount: Mount, guideOutput: GuideOutput,
        guideStartLooping: GuideStartLooping,
    ) {
        if (isLooping()) return
        if (!camera.connected) return

        guideCamera.set(camera)
        guideMount.set(mount)
        this.guideOutput.set(guideOutput)

        with(guider) {
            searchRegion = guideStartLooping.searchRegion
            dither = if (guideStartLooping.ditherMode == DitherMode.RANDOM) randomDither else spiralDither
            ditherAmount = guideStartLooping.ditherAmount
            ditherRAOnly = guideStartLooping.ditherRAOnly
            calibrationFlipRequiresDecFlip = guideStartLooping.calibrationFlipRequiresDecFlip
            assumeDECOrthogonalToRA = guideStartLooping.assumeDECOrthogonalToRA
            calibrationStep = guideStartLooping.calibrationStep
            calibrationDistance = guideStartLooping.calibrationDistance
            useDECCompensation = guideStartLooping.useDECCompensation
            declinationGuideMode = guideStartLooping.declinationGuideMode
            maxDECDuration = guideStartLooping.maxDECDuration
            maxRADuration = guideStartLooping.maxRADuration
            noiseReductionMethod = guideStartLooping.noiseReductionMethod
            xGuideAlgorithm = xGuideAlgorithms[guideStartLooping.xGuideAlgorithm]
            yGuideAlgorithm = yGuideAlgorithms[guideStartLooping.yGuideAlgorithm]
        }

        camera.enableBlob()

        val guidingTasklet = GuidingTasklet(camera, guider, guideStartLooping)

        val guidingStep = StepBuilder("GuidingStep.${camera.name}.${stepExecutionCounter.getAndIncrement()}", jobRepository)
            .tasklet(guidingTasklet, platformTransactionManager)
            // .listener(this)
            .build()

        val job = JobBuilder("GuidingJob.${jobExecutionCounter.getAndIncrement()}", jobRepository)
            .start(guidingStep)
            // .listener(this)
            .listener(guidingTasklet)
            .build()

        asyncJobLauncher.run(job, JobParameters())
            .also { runningJob.set(job to it) }
            .also { jobRegistry.register(ReferenceJobFactory(job)) }
    }

    fun isLooping(): Boolean {
        return runningJob.get()?.second?.isRunning ?: false
    }

    fun isGuiding(): Boolean {
        return guider.isGuiding
    }

    fun selectGuideStar(x: Double, y: Double) {
        guider.selectGuideStar(x, y)
    }

    fun deselectGuideStar() {
        guider.deselectGuideStar()
    }

    @Synchronized
    fun startGuiding(forceCalibration: Boolean = false) {
        if (guider.isGuiding) return
        if (!guideMount.get().connected || !guideOutput.get().connected) return

        val calibration = guideCalibrationRepository
            .get(guideCamera.get(), guideMount.get(), guideOutput.get())
            ?.toGuideCalibration()

        if (forceCalibration || calibration == null) {
            LOG.info("starting guiding with force calibration")
            guider.clearCalibration()
        } else {
            LOG.info("calibration restored. calibration={}", calibration)
            guider.loadCalibration(calibration)
        }

        guider.startGuiding()
    }

    @Synchronized
    fun stop() {
        val jobExecution = runningJob.get()?.second ?: return
        jobOperator.stop(jobExecution.jobId)
    }

    override fun onLockPositionChanged(position: GuidePoint) {
        messageService.sendMessage(GUIDE_LOCK_POSITION_CHANGED, guider)
    }

    override fun onStarSelected(star: StarPoint) {}

    override fun onGuidingDithered(dx: Double, dy: Double) {}

    override fun onGuidingStopped() {}

    override fun onLockShiftLimitReached() {}

    override fun onLooping(image: Image, number: Int, star: StarPoint?) {}

    override fun onStarLost() {
        messageService.sendMessage(GUIDE_STAR_LOST, guider)
    }

    override fun onLockPositionLost() {
        messageService.sendMessage(GUIDE_LOCK_POSITION_LOST, guider)
    }

    override fun onStartCalibration() {}

    override fun onCalibrationStep(
        calibrationState: CalibrationState, direction: GuideDirection, stepNumber: Int,
        dx: Double, dy: Double,
        posX: Double, posY: Double,
        distance: Double,
    ) {
    }

    override fun onCalibrationCompleted(calibration: GuideCalibration) {
        guideCalibrationRepository
            .save(GuideCalibrationEntity.from(guideCamera.get(), guideMount.get(), guideOutput.get(), calibration))
    }

    override fun onCalibrationFailed() {}

    override fun onGuideStep(stats: GuideStats) {}

    override fun onNotifyDirectMove(mount: GuidePoint) {}

    companion object {

        @JvmStatic private val LOG = loggerFor<GuidingExecutor>()

        const val GUIDE_EXPOSURE_FINISHED = "GUIDE_EXPOSURE_FINISHED"
        const val GUIDE_LOCK_POSITION_CHANGED = "GUIDE_LOCK_POSITION_CHANGED"
        const val GUIDE_STAR_LOST = "GUIDE_STAR_LOST"
        const val GUIDE_LOCK_POSITION_LOST = "GUIDE_LOCK_POSITION_LOST"
    }
}
