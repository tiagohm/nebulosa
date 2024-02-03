package nebulosa.api.alignment.polar.tppa

import nebulosa.api.jobs.JobExecutor
import nebulosa.api.messages.MessageService
import nebulosa.api.solver.PlateSolverService
import nebulosa.batch.processing.JobLauncher
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount
import nebulosa.log.info
import nebulosa.log.loggerFor
import nebulosa.star.detection.StarDetector
import org.springframework.stereotype.Component

@Component
class TPPAExecutor(
    override val jobLauncher: JobLauncher,
    private val messageService: MessageService,
    private val plateSolverService: PlateSolverService,
    private val starDetector: StarDetector<Image>,
) : JobExecutor() {

    @Synchronized
    fun execute(camera: Camera, mount: Mount, request: TPPAStartRequest): String {
        check(findJobExecutionWithAny(camera, mount) == null) { "TPPA job is already running" }

        LOG.info { "starting TPPA. camera=$camera, mount=$mount, request=$request" }

        val solver = plateSolverService.solverFor(request.plateSolverOptions)

        return with(TPPAJob(camera, request, solver, starDetector, mount)) {
            subscribe(messageService::sendMessage)
            register(jobLauncher.launch(this))
            id
        }
    }

    fun stop(camera: Camera, mount: Mount) {
        stopWithAny(camera, mount)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<TPPAExecutor>()
    }
}
