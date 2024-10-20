package nebulosa.util.concurrency.cancellation

import nebulosa.util.concurrency.latch.Pauser
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class CancellationToken private constructor(private val completable: CompletableFuture<CancellationSource>?) :
    Pauser(), Cancellable, Future<CancellationSource>, AutoCloseable {

    constructor() : this(CompletableFuture<CancellationSource>())

    private val listeners = LinkedHashSet<CancellationListener>()

    init {
        completable?.whenComplete { source, _ ->
            synchronized(this) {
                unpause()

                if (source != null) {
                    listeners.forEach { it.onCancel(source) }
                }

                listeners.clear()
            }
        }
    }

    @Synchronized
    fun listen(listener: CancellationListener) {
        if (completable != null) {
            if (isDone) {
                listener.onCancel(CancellationSource.Listen)
            } else {
                listeners.add(listener)
            }
        }
    }

    @Synchronized
    fun unlisten(listener: CancellationListener) {
        listeners.remove(listener)
    }

    @Synchronized
    fun unlistenAll() {
        listeners.clear()
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return cancel(CancellationSource.Cancel(mayInterruptIfRunning))
    }

    @Synchronized
    override fun cancel(source: CancellationSource): Boolean {
        unpause()
        completable?.complete(source) ?: return false
        return true
    }

    override fun isCancelled(): Boolean {
        return completable != null && isDone
    }

    override fun isDone(): Boolean {
        return completable?.isDone != false
    }

    override fun get(): CancellationSource {
        return completable?.get() ?: CancellationSource.None
    }

    override fun get(timeout: Long, unit: TimeUnit): CancellationSource {
        return completable?.get(timeout, unit) ?: CancellationSource.None
    }

    fun throwIfCancelled() {
        if (isCancelled) {
            throw CancellationException()
        }
    }

    override fun close() {
        super.close()

        if (!isDone) {
            completable?.complete(CancellationSource.Close)
        }

        unlistenAll()
    }

    companion object {

        @JvmStatic val NONE = CancellationToken(null)
    }
}
