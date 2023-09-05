package nebulosa.api.sequencer.executor

import org.springframework.core.task.AsyncTaskExecutor
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class ExecutorServiceTaskExecutor(private val executorService: ExecutorService) : AsyncTaskExecutor {

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Deprecated as of 5.3.16 since the common executors do not support start timeouts")
    override fun execute(task: Runnable, startTimeout: Long) {
        TODO("Not yet implemented")
    }

    override fun execute(task: Runnable) {
        submit(task)
    }

    override fun submit(task: Runnable): Future<*> {
        return executorService.submit(task)
    }

    override fun <T> submit(task: Callable<T>): Future<T> {
        return executorService.submit(task)
    }
}
