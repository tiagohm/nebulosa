package nebulosa.api.wizard.flat

import nebulosa.api.jobs.JobExecutor
import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecution
import nebulosa.batch.processing.JobLauncher
import nebulosa.indi.device.camera.Camera
import nebulosa.log.info
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component

@Component
class FlatWizardExecutor(
    private val messageService: MessageService,
    override val jobLauncher: JobLauncher,
) : JobExecutor() {

    fun execute(camera: Camera, request: FlatWizardRequest) {
        check(camera.connected) { "camera is not connected" }
        check(findJobExecution(camera) == null) { "job is already running for camera: [${camera.name}]" }

        LOG.info { "starting flat wizard capture. camera=$camera, request=$request" }

        val flatWizardJob = FlatWizardJob(camera, request)
        flatWizardJob.subscribe(messageService::sendMessage)
        register(jobLauncher.launch(flatWizardJob))
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

    fun stop(camera: Camera) {
        stopWithAny(camera)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FlatWizardExecutor>()
    }
}
