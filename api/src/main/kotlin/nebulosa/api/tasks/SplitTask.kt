package nebulosa.api.tasks

import nebulosa.common.concurrency.cancel.CancellationToken
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

open class SplitTask(
    private val tasks: Array<Task<*>>,
    private val executor: Executor = EXECUTOR,
) : Task<Any>() {

    override fun execute(cancellationToken: CancellationToken) {
        if (tasks.isEmpty()) {
            return
        } else if (tasks.size == 1) {
            tasks[0].execute(cancellationToken)
        } else {
            val completables = Array(tasks.size) { CompletableFuture.runAsync({ tasks[it].execute(cancellationToken) }, executor) }
            CompletableFuture.allOf(*completables).join()
        }
    }

    companion object {

        @JvmStatic val EXECUTOR: Executor = ForkJoinPool.commonPool()
    }
}
