package nebulosa.desktop.logic.concurrency

import javafx.application.Platform
import java.util.concurrent.*

class JavaFXExecutorService : ExecutorService {

    override fun execute(command: Runnable) {
        runOnJavaFXThread(command)
    }

    override fun <T : Any> submit(task: Callable<T>): Future<T> {
        val future = CompletableFuture<T>()

        runOnJavaFXThread {
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

        runOnJavaFXThread {
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

        runOnJavaFXThread {
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

    override fun <T : Any> invokeAll(tasks: MutableCollection<out Callable<T>>): MutableList<Future<T>> {
        TODO("Not yet implemented")
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

    companion object {

        @JvmStatic
        @Suppress("NOTHING_TO_INLINE")
        inline fun runOnJavaFXThread(command: Runnable) {
            if (Platform.isFxApplicationThread()) command.run()
            else Platform.runLater(command)
        }

        @JvmStatic
        inline fun runOnJavaFXThread(crossinline block: () -> Unit) {
            if (Platform.isFxApplicationThread()) block()
            else Platform.runLater { block() }
        }
    }
}
