package nebulosa.job.manager

import nebulosa.util.Stoppable
import nebulosa.util.concurrency.latch.Pauseable
import java.util.function.Consumer

interface Job : MutableCollection<Task>, Runnable, Pauseable, Stoppable, Consumer<Any> {

    val loopCount: Int

    val taskCount: Int

    val isRunning: Boolean

    val isCancelled: Boolean

    val currentTask: Task?

    fun runTask(task: Task, prev: Task?): TaskExecutionState
}
