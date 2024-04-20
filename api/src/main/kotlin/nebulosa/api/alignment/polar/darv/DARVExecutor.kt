package nebulosa.api.alignment.polar.darv

import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobLauncher
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.stereotype.Component

/**
 * @see <a href="https://www.cloudynights.com/articles/cat/articles/darv-drift-alignment-by-robert-vice-r2760">Reference</a>
 */
@Component
class DARVExecutor(
    private val messageService: MessageService,
) {

    @Synchronized
    fun execute(camera: Camera, guideOutput: GuideOutput, request: DARVStartRequest): String {
        val darvJob = DARVJob(camera, guideOutput, request)
        darvJob.subscribe(messageService::sendMessage)
        register(jobLauncher.launch(darvJob))
        return darvJob.id
    }
}
