package nebulosa.api.sequencer

import nebulosa.indi.device.Device
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution

interface SequenceJob {

    val devices: List<Device>

    val job: Job

    val jobExecution: JobExecution

    val jobId
        get() = jobExecution.jobId

    val startTime
        get() = jobExecution.startTime

    val endTime
        get() = jobExecution.endTime

    val isRunning
        get() = jobExecution.isRunning
}
