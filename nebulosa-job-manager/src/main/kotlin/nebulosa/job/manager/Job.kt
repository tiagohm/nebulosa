package nebulosa.job.manager

import nebulosa.util.Stoppable
import nebulosa.util.concurrency.latch.Pauseable
import java.util.Deque
import java.util.function.Consumer

interface Job : Deque<Task>, Runnable, Pauseable, Stoppable, Consumer<Any> {

    val loopCount: Int

    val taskCount: Int

    val isRunning: Boolean

    val isCancelled: Boolean

    val currentTask: Task?

    fun waitForPause()

    fun runTask(task: Task, prev: Task?): TaskExecutionState
}
