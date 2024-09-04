package nebulosa.job.manager

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

data class SplitTask(
    private val tasks: List<Task>,
    private val executor: Executor = EXECUTOR,
) : Task {

    override fun run() {
        if (tasks.isEmpty()) {
            return
        } else if (tasks.size == 1) {
            tasks[0].run()
        } else {
            val completables = Array(tasks.size) { CompletableFuture.runAsync(tasks[it], executor) }
            CompletableFuture.allOf(*completables).join()
        }
    }

    companion object {

        @JvmStatic private val EXECUTOR = ForkJoinPool.commonPool()
    }
}
