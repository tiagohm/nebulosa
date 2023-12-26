package nebulosa.common.concurrency

import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class CancellationToken private constructor(private val completable: CompletableFuture<CancellationSource>?) : Closeable, Future<CancellationSource> {

    constructor() : this(CompletableFuture<CancellationSource>())

    private val listeners = LinkedHashSet<CancellationListener>()

    init {
        completable?.whenComplete { source, _ ->
            if (source != null) {
                listeners.forEach { it.accept(source) }
            }

            listeners.clear()
        }
    }

    fun listen(listener: CancellationListener): Boolean {
        return if (completable == null) {
            false
        } else if (isDone) {
            listener.accept(CancellationSource.Listen)
            false
        } else {
            listeners.add(listener)
        }
    }

    fun unlisten(listener: CancellationListener): Boolean {
        return listeners.remove(listener)
    }

    fun cancel() {
        cancel(true)
    }

    @Synchronized
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        completable?.complete(CancellationSource.Cancel(mayInterruptIfRunning)) ?: return false
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
        if (!isDone) {
            completable?.complete(CancellationSource.Close)
        }
    }

    companion object {

        @JvmStatic val NONE = CancellationToken(null)
    }
}
