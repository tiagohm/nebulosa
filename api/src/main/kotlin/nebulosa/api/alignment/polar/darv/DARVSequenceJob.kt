package nebulosa.api.alignment.polar.darv

import nebulosa.api.sequencer.SequenceJob
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution

data class DARVSequenceJob(
    val camera: Camera,
    val guideOutput: GuideOutput,
    val data: DARVStart,
    override val job: Job,
    override val jobExecution: JobExecution,
) : SequenceJob {

    override val devices = listOf(camera, guideOutput)
}
