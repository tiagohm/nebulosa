package nebulosa.api.alignment.polar.tppa

import nebulosa.api.messages.MessageService
import nebulosa.api.solver.PlateSolverService
import nebulosa.batch.processing.JobExecutor
import nebulosa.batch.processing.JobLauncher
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount
import nebulosa.log.info
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component

@Component
class TPPAExecutor(
    override val jobLauncher: JobLauncher,
    private val messageService: MessageService,
    private val plateSolverService: PlateSolverService,
) : JobExecutor() {

    @Synchronized
    fun execute(camera: Camera, mount: Mount, request: TPPAStartRequest): String {
        check(findJobExecutionWithAny(camera, mount) == null) { "TPPA job is already running" }

        LOG.info { "starting TPPA. camera=$camera, mount=$mount, request=$request" }

        val solver = plateSolverService.solverFor(request.plateSolver)

        return with(TPPAJob(camera, request, solver, mount)) {
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
