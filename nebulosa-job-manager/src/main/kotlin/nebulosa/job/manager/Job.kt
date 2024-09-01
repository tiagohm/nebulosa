package nebulosa.job.manager

import java.util.Deque
import java.util.function.Consumer

interface Job : Deque<Task>, Runnable, Consumer<Any> {

    val loopCount: Int

    val taskCount: Int

    val isRunning: Boolean

    val isCancelled: Boolean

    val isPaused: Boolean

    val currentTask: Task?

    fun waitForPause()
}
