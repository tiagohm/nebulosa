package nebulosa.desktop.equipments

import java.util.concurrent.CompletableFuture

abstract class ThreadedTask<T> : CompletableFuture<T>(), Runnable {

    abstract fun execute(): T

    abstract fun finishGracefully()

    final override fun run() {
        try {
            complete(execute())
        } catch (e: InterruptedException) {
            println("${this::class} was interrupted")
        } catch (e: Throwable) {
            completeExceptionally(e)
        }
    }

    fun runOnThread(): Thread {
        val thread = Thread(this)
        thread.start()
        return thread
    }
}
