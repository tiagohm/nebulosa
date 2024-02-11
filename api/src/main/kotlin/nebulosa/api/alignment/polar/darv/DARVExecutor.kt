package nebulosa.api.alignment.polar.darv

import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecutor
import nebulosa.batch.processing.JobLauncher
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.log.info
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component

/**
 * @see <a href="https://www.cloudynights.com/articles/cat/articles/darv-drift-alignment-by-robert-vice-r2760">Reference</a>
 */
@Component
class DARVExecutor(
    override val jobLauncher: JobLauncher,
    private val messageService: MessageService,
) : JobExecutor() {

    @Synchronized
    fun execute(camera: Camera, guideOutput: GuideOutput, request: DARVStartRequest): String {
        check(findJobExecutionWithAny(camera, guideOutput) == null) { "DARV job is already running" }

        LOG.info { "starting DARV. camera=$camera, guideOutput=$guideOutput, request=$request" }

        return with(DARVJob(camera, guideOutput, request)) {
            subscribe(messageService::sendMessage)
            register(jobLauncher.launch(this))
            id
        }
    }

    fun stop(camera: Camera, guideOutput: GuideOutput) {
        stopWithAny(camera, guideOutput)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<DARVExecutor>()
    }
}
