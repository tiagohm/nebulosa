package nebulosa.api.sequencer

import nebulosa.api.messages.MessageService
import nebulosa.batch.processing.JobExecutor
import nebulosa.batch.processing.JobLauncher
import nebulosa.guiding.Guider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.log.info
import nebulosa.log.loggerFor
import org.springframework.stereotype.Component

@Component
class SequencerExecutor(
    private val messageService: MessageService,
    private val guider: Guider,
    override val jobLauncher: JobLauncher,
) : JobExecutor() {

    fun execute(
        camera: Camera, request: SequencePlanRequest,
        wheel: FilterWheel? = null, focuser: Focuser? = null,
    ): String {
        check(findJobExecutionWithAny(camera) == null) { "job is already running" }

        LOG.info { "starting sequencer. camera=$camera, wheel=$wheel, focuser=$focuser, request=$request" }

        val sequencerJob = SequencerJob(camera, request, guider, wheel, focuser)
        sequencerJob.subscribe(messageService::sendMessage)
        sequencerJob.initialize()
        register(jobLauncher.launch(sequencerJob))
        return sequencerJob.id
    }

    fun stop(camera: Camera) {
        findJobExecutionWithAny(camera)?.also { jobLauncher.stop(it) }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SequencerExecutor>()
    }
}
