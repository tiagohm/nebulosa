package nebulosa.api.alignment.polar.darv

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.log.debug
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component
import java.util.*

/**
 * @see <a href="https://www.cloudynights.com/articles/cat/articles/darv-drift-alignment-by-robert-vice-r2760">Reference</a>
 */
@Component
class DARVExecutor(
    private val jobLauncher: JobLauncher,
    private val messageService: MessageService,
) : Consumer<MessageEvent> {

    private val jobExecutions = LinkedList<JobExecution>()

    @Synchronized
    fun execute(request: DARVStartRequest) {
        val camera = requireNotNull(request.camera)
        val guideOutput = requireNotNull(request.guideOutput)

        check(!isRunning(camera, guideOutput)) { "DARV job is already running" }

        LOG.debug { "starting DARV. request=%s".format(request) }

        with(DARVJob(request)) {
            subscribe(this@DARVExecutor)
            val jobExecution = jobLauncher.launch(this)
            jobExecutions.add(jobExecution)
        }
    }

    fun findJobExecution(camera: Camera, guideOutput: GuideOutput): JobExecution? {
        for (i in jobExecutions.indices.reversed()) {
            val jobExecution = jobExecutions[i]
            val job = jobExecution.job as DARVJob

            if (!jobExecution.isDone && job.camera === camera && job.guideOutput === guideOutput) {
                return jobExecution
            }
        }

        return null
    }

    @Synchronized
    fun stop(camera: Camera, guideOutput: GuideOutput) {
        val jobExecution = findJobExecution(camera, guideOutput) ?: return
        jobLauncher.stop(jobExecution)
    }

    fun isRunning(camera: Camera, guideOutput: GuideOutput): Boolean {
        return findJobExecution(camera, guideOutput) != null
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVExecutor>()
    }
}
