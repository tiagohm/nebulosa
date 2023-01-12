package nebulosa.desktop.equipments

import java.util.*

abstract class ThreadedTaskManager<T, R : ThreadedTask<T>> : LinkedList<R>() {

    inline val currentTask get() = firstOrNull()

    val isRunning get() = currentTask?.let { !it.isDone } ?: false

    val isDone get() = currentTask?.isDone ?: true

    @Synchronized
    fun execute(
        task: R,
        action: (Result<T>) -> Unit = {},
    ): Boolean {
        if (isRunning) return false

        addFirst(task)

        task.execute()
            .whenComplete { data, e ->
                action(if (data != null) Result.success(data) else Result.failure(e))
            }

        return true
    }

    fun cancel() {
        currentTask?.cancel(true)
    }
}
