package nebulosa.api.guiding

import nebulosa.job.manager.Job

data class WaitForSettleFinished(override val job: Job, override val task: WaitForSettleTask) : WaitForSettleEvent
