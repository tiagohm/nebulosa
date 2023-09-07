package nebulosa.api.guiding

import jakarta.annotation.PostConstruct
import nebulosa.guiding.internal.*
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.atomic.AtomicReference

@Component
class GuidingExecutor(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val cameraJobLauncher: JobLauncher,
    private val platformTransactionManager: PlatformTransactionManager,
    private val jobRegistry: JobRegistry,
) : GuidingCamera, GuidingMount, GuidingRotator, GuidingPulse {

    private val guider = MultiStarGuider()
    private val guideCamera = AtomicReference<Camera>()
    private val guideMount = AtomicReference<Mount>()
    private val guideOutput = AtomicReference<GuideOutput>()

    @PostConstruct
    private fun initialize() {
        guider.camera = this
    }

    override val binning: Int
        get() = TODO("Not yet implemented")

    override val pixelScale: Double
        get() = TODO("Not yet implemented")

    override val exposureTime: Long
        get() = TODO("Not yet implemented")

    override val isBusy: Boolean
        get() = TODO("Not yet implemented")

    override val rightAscension: Angle
        get() = TODO("Not yet implemented")

    override val declination: Angle
        get() = TODO("Not yet implemented")

    override val rightAscensionGuideRate: Double
        get() = TODO("Not yet implemented")

    override val declinationGuideRate: Double
        get() = TODO("Not yet implemented")

    override val isPierSideAtEast: Boolean
        get() = TODO("Not yet implemented")

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
    override val angle = Angle.ZERO

    fun startLooping(camera: Camera, mount: Mount, guideOutput: GuideOutput) {
        if (guider.isLooping) return
        if (!camera.connected) return

        guideCamera.set(camera)
        guideMount.set(mount)
        this.guideOutput.set(guideOutput)

        camera.enableBlob()
        guider.startLooping()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<GuidingExecutor>()
    }
}
