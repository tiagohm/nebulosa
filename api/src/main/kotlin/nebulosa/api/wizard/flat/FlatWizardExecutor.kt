package nebulosa.api.wizard.flat

import io.reactivex.rxjava3.functions.Consumer
import nebulosa.api.messages.MessageEvent
import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.log.debug
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component
import java.util.*

@Component
class FlatWizardExecutor(
    private val messageService: MessageService,
    private val jobLauncher: JobLauncher,
) : Consumer<MessageEvent> {

    private val jobExecutions = LinkedList<JobExecution>()

    fun execute(request: FlatWizardRequest) {
        val camera = requireNotNull(request.captureRequest.camera)

        check(camera.connected) { "camera is not connected" }
        check(!isCapturing(camera)) { "job is already running for camera: [${camera.name}]" }

        LOG.debug { "starting flat wizard capture. request=$request" }

        val flatWizardJob = FlatWizardJob(request)
        flatWizardJob.subscribe(this)
        jobExecutions.add(jobLauncher.launch(flatWizardJob))
    }

    fun findJobExecution(camera: Camera): JobExecution? {
        for (i in jobExecutions.indices.reversed()) {
            val jobExecution = jobExecutions[i]
            val job = jobExecution.job as FlatWizardJob

            if (!jobExecution.isDone && job.camera === camera) {
                return jobExecution
            }
        }

        return null
    }

    @Synchronized
    fun stop(camera: Camera) {
        val jobExecution = findJobExecution(camera) ?: return
        jobLauncher.stop(jobExecution)
    }

    fun isCapturing(camera: Camera, wheel: FilterWheel? = null): Boolean {
        return findJobExecution(camera) != null
    }

    override fun accept(event: MessageEvent) {
        messageService.sendMessage(event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FlatWizardExecutor>()
    }
}
