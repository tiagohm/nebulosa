package nebulosa.desktop.equipments

import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Supplier

abstract class ThreadedTask<T> : CompletableFuture<T>(), Supplier<T> {

    abstract fun finishGracefully()

    fun execute() = supplyAsync(this, EXECUTOR)!!

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(ThreadedTask::class.java)
        @JvmStatic private val EXECUTOR = Executors.newFixedThreadPool(8)
    }
}
