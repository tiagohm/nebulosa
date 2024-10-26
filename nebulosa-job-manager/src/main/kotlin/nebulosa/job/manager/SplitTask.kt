package nebulosa.job.manager

import nebulosa.util.concurrency.cancellation.CancellationSource
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

data class SplitTask(
    private val tasks: List<Task>,
    private val executor: Executor = EXECUTOR,
) : Task {

    override fun onPause(paused: Boolean) {
        tasks.forEach { it.onPause(paused) }
    }

    override fun onCancel(source: CancellationSource) {
        tasks.forEach { it.onCancel(source) }
    }

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

        private val EXECUTOR = ForkJoinPool.commonPool()
    }
}
