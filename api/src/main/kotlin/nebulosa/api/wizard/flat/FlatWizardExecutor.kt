package nebulosa.api.wizard.flat

import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecutor
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
        check(findJobExecutionWithAny(camera) == null) { "job is already running for camera: [${camera.name}]" }

        LOG.info { "starting flat wizard capture. camera=$camera, request=$request" }

        val flatWizardJob = FlatWizardJob(camera, request)
        flatWizardJob.subscribe(messageService::sendMessage)
        register(jobLauncher.launch(flatWizardJob))
    }

    fun stop(camera: Camera) {
        stopWithAny(camera)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FlatWizardExecutor>()
    }
}
