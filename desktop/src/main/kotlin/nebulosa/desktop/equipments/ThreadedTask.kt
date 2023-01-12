package nebulosa.desktop.equipments

import nebulosa.desktop.core.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Phaser

abstract class ThreadedTask<T> : CompletableFuture<T>(), Callable<T>, KoinComponent {

    protected val eventBus by inject<EventBus>()

    private val phaser = Phaser(1)

    abstract fun finishGracefully()

    private fun completeOrThrow() = try {
        complete(call())
    } catch (e: Throwable) {
        completeExceptionally(e)
    }

    fun execute() = apply { supplyAsync(::completeOrThrow, EXECUTOR)!! }

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
