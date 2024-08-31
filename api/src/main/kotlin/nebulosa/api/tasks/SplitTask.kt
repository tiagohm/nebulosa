package nebulosa.api.tasks

import nebulosa.util.concurrency.cancellation.CancellationToken
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

data class SplitTask(
    private val tasks: Collection<Task>,
    private val executor: Executor? = null,
) : Task {

    override fun execute(cancellationToken: CancellationToken) {
        if (tasks.isEmpty()) {
            return
        } else if (tasks.size == 1) {
            tasks.first().execute(cancellationToken)
        } else {
            val completables = tasks.map { CompletableFuture.runAsync({ it.execute(cancellationToken) }, executor ?: EXECUTOR) }
            completables.forEach(CompletableFuture<*>::join)
        }
    }

    override fun reset() {
        tasks.forEach { it.reset() }
    }

    companion object {

        @JvmStatic private val EXECUTOR = ForkJoinPool.commonPool()
    }
}
