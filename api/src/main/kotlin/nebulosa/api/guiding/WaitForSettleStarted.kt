package nebulosa.api.guiding

import nebulosa.job.manager.Job

data class WaitForSettleStarted(override val job: Job, override val task: WaitForSettleTask) : WaitForSettleEvent
