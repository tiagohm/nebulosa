package nebulosa.common.concurrency.cancel

import nebulosa.common.concurrency.latch.Pauser
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

typealias CancellationListener = Consumer<CancellationSource>

class CancellationToken private constructor(private val completable: CompletableFuture<CancellationSource>?) : Pauser(), Closeable,
    Future<CancellationSource> {

    constructor() : this(CompletableFuture<CancellationSource>())

    private val listeners = LinkedHashSet<CancellationListener>()

    init {
        completable?.whenComplete { source, _ ->
            synchronized(this) {
                unpause()

                if (source != null) {
                    listeners.forEach { it.accept(source) }
                }

                listeners.clear()
            }
        }
    }

    @Synchronized
    fun listen(listener: CancellationListener) {
        if (completable != null) {
            if (isDone) {
                listener.accept(CancellationSource.Listen)
            } else {
                listeners.add(listener)
            }
        }
    }

    @Synchronized
    fun unlisten(listener: CancellationListener) {
        listeners.remove(listener)
    }

    fun cancel() {
        cancel(true)
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return cancel(CancellationSource.Cancel(mayInterruptIfRunning))
    }

    @Synchronized
    fun cancel(source: CancellationSource): Boolean {
        unpause()
        completable?.complete(source) ?: return false
        return true
    }

    override fun isCancelled(): Boolean {
        return isDone
    }

    override fun isDone(): Boolean {
        return completable?.isDone ?: true
    }

    override fun get(): CancellationSource {
        return completable?.get() ?: CancellationSource.None
    }

    override fun get(timeout: Long, unit: TimeUnit): CancellationSource {
        return completable?.get(timeout, unit) ?: CancellationSource.None
    }

    override fun close() {
        super.close()

        if (!isDone) {
            completable?.complete(CancellationSource.Close)
        }
    }

    companion object {

        @JvmStatic val NONE = CancellationToken(null)
    }
}
