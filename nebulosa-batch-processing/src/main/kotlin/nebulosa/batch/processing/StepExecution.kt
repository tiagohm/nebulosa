package nebulosa.batch.processing

import java.time.LocalDateTime

data class StepExecution(
    val step: Step,
    val jobExecution: JobExecution,
    val startedAt: LocalDateTime = LocalDateTime.now(),
    var finishedAt: LocalDateTime? = null,
)
