package nebulosa.api.tasks

import nebulosa.common.concurrency.cancel.CancellationToken
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

open class SplitTask(
    private val tasks: Collection<Task>,
    private val executor: Executor = EXECUTOR,
) : Task {

    override fun execute(cancellationToken: CancellationToken) {
        if (tasks.isEmpty()) {
            return
        } else if (tasks.size == 1) {
            tasks.first().execute(cancellationToken)
        } else {
            val completables = tasks.map { CompletableFuture.runAsync({ it.execute(cancellationToken) }, executor) }
            completables.forEach(CompletableFuture<*>::join)
        }
    }

    override fun reset() {
        tasks.forEach { it.reset() }
    }

    companion object {

        @JvmStatic val EXECUTOR: Executor = ForkJoinPool.commonPool()
    }
}
