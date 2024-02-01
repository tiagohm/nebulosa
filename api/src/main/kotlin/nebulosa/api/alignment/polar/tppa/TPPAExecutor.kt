package nebulosa.api.alignment.polar.tppa

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.MessageService
import nebulosa.api.solver.PlateSolverService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.star.detection.StarDetector
import org.springframework.stereotype.Component
import java.util.*

@Component
class TPPAExecutor(
    private val jobLauncher: JobLauncher,
    private val messageService: MessageService,
    private val plateSolverService: PlateSolverService,
    private val starDetector: StarDetector<Image>,
) : Consumer<MessageEvent> {

    private val jobExecutions = LinkedList<JobExecution>()

    @Synchronized
    fun execute(request: TPPAStartRequest) {
        val camera = requireNotNull(request.camera)
        val mount = requireNotNull(request.mount)

        check(!isRunning(camera, mount)) { "TPPA job is already running" }

        LOG.debug { "starting TPPA. request=%s".format(request) }

        val solver = plateSolverService.solverFor(request.plateSolverOptions)

        with(TPPAJob(request, solver, starDetector)) {
            subscribe(this@TPPAExecutor)
            val jobExecution = jobLauncher.launch(this)
            jobExecutions.add(jobExecution)
        }
    }

    fun findJobExecution(camera: Camera, mount: Mount): JobExecution? {
        for (i in jobExecutions.indices.reversed()) {
            val jobExecution = jobExecutions[i]
            val job = jobExecution.job as TPPAJob

            if (!jobExecution.isDone && job.camera === camera && job.mount === mount) {
                return jobExecution
            }
        }

        return null
    }

    @Synchronized
    fun stop(camera: Camera, mount: Mount) {
        val jobExecution = findJobExecution(camera, mount) ?: return
        jobLauncher.stop(jobExecution)
    }

    fun isRunning(camera: Camera, mount: Mount): Boolean {
        return findJobExecution(camera, mount) != null
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<TPPAExecutor>()
    }
}
