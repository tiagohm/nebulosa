package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.cameras.CameraExposureFinished
import nebulosa.api.messages.MessageEvent
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
) : JobExecutor(), Consumer<MessageEvent> {

    @Synchronized
    fun execute(camera: Camera, mount: Mount, request: TPPAStartRequest): String {
        check(findJobExecutionWithAny(camera, mount) == null) { "TPPA job is already running" }

        LOG.info { "starting TPPA. camera=$camera, mount=$mount, request=$request" }

        val solver = plateSolverService.solverFor(request.plateSolver)

        val tppaJob = TPPAJob(camera, request, solver, mount)
        tppaJob.subscribe(this)
        register(jobLauncher.launch(tppaJob))
        return tppaJob.id
    }

    override fun accept(event: MessageEvent) {
        if (event is TPPAEvent || event is CameraExposureFinished) {
            messageService.sendMessage(event)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<TPPAExecutor>()
    }
}
