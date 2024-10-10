package nebulosa.api.guiding

import nebulosa.job.manager.Job

data class DitherAfterExposureDithered(
    override val job: Job,
    override val task: DitherAfterExposureTask,
    override val dx: Double,
    override val dy: Double,
) : DitherAfterExposureEvent
