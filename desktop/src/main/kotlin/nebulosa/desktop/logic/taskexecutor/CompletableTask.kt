package nebulosa.desktop.logic.taskexecutor

import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class CompletableTask(
    @JvmField internal val task: Task,
    private val completable: CompletableFuture<Any>,
) : Runnable, Closeable, Future<Any> by completable {

    override fun run() {
        try {
            completable.complete(task.call())
        } catch (e: InterruptedException) {
            throw e
        } catch (e: Throwable) {
            completable.completeExceptionally(e)
        }
    }

    override fun close() = task.closeGracefully()
}
