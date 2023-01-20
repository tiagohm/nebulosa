package nebulosa.desktop.logic.task

import java.io.Closeable
import java.util.concurrent.CompletableFuture

class CompletableTask(@JvmField internal val task: Task) : Runnable, Closeable, CompletableFuture<Any>() {

    override fun run() {
        try {
            complete(task.call())
        } catch (e: InterruptedException) {
            throw e
        } catch (e: Throwable) {
            completeExceptionally(e)
        }
    }

    override fun close() = task.closeGracefully()
}
