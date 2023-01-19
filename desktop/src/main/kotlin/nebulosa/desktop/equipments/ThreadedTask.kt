package nebulosa.desktop.equipments

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Phaser

abstract class ThreadedTask<T> : CompletableFuture<T>(), Callable<T> {

    private val phaser = Phaser(1)

    abstract fun finishGracefully()

    fun execute() = apply {
        supplyAsync({
            try {
                complete(call())
            } catch (e: Throwable) {
                completeExceptionally(e)
            }
        }, EXECUTOR)!!
    }

    protected fun finish() {
        phaser.forceTermination()
        finishGracefully()
    }

    protected fun acquire() = phaser.register()

    protected fun release() = phaser.arriveAndDeregister()

    protected fun await() = phaser.arriveAndAwaitAdvance()

    companion object {

        @JvmStatic private val EXECUTOR = Executors.newFixedThreadPool(8)
    }
}
