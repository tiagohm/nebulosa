package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceJob
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution

data class CameraSequenceJob(
    val camera: Camera,
    val data: CameraStartCapture,
    override val job: Job,
    override val jobExecution: JobExecution,
) : SequenceJob {

    override val devices = listOf(camera)
}
