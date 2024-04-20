package nebulosa.api.cameras

import nebulosa.api.guiding.WaitForSettleTask
import nebulosa.api.tasks.SplitTask
import nebulosa.api.tasks.delay.DelayTask

data class DelayAndWaitForSettleTask(
    private val delayTask: DelayTask,
    private val waitForSettleTask: WaitForSettleTask,
) : SplitTask(arrayOf(delayTask, waitForSettleTask))
