package nebulosa.desktop.logic.concurrency

import java.util.concurrent.*

class JavaFXExecutorService(private val executor: Executor) : ExecutorService {

    override fun execute(command: Runnable) {
        executor.execute(command)
    }

    override fun <T : Any> submit(task: Callable<T>): Future<T> {
        val future = CompletableFuture<T>()

        executor.execute {
            try {
                future.complete(task.call())
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    override fun <T : Any> submit(task: Runnable, result: T): Future<T> {
        val future = CompletableFuture<T>()

        executor.execute {
            try {
                task.run()
                future.complete(result)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    override fun submit(task: Runnable): Future<*> {
        val future = CompletableFuture<Unit>()

        executor.execute {
            try {
                task.run()
                future.complete(Unit)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    override fun shutdown() {}

    override fun shutdownNow() = emptyList<Runnable>()

    override fun isShutdown() = false

    override fun isTerminated() = false

    override fun awaitTermination(timeout: Long, unit: TimeUnit) = true

    override fun <T : Any> invokeAll(tasks: MutableCollection<out Callable<T>>): List<Future<T>> {
        return tasks.map { submit(it) }
    }

    override fun <T : Any> invokeAll(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): MutableList<Future<T>> {
        TODO("Not yet implemented")
    }

    override fun <T : Any> invokeAny(tasks: MutableCollection<out Callable<T>>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> invokeAny(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): T {
        TODO("Not yet implemented")
    }
}
