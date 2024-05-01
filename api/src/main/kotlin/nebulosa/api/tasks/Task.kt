package nebulosa.api.tasks

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import nebulosa.common.Resettable
import nebulosa.common.concurrency.cancel.CancellationToken
import java.io.Closeable

abstract class Task<T : Any> : Observable<T>(), Resettable, Closeable {

    private val observers = HashSet<Observer<in T>>(1)

    abstract fun execute(cancellationToken: CancellationToken = CancellationToken.NONE)

    override fun reset() = Unit

    final override fun subscribeActual(observer: Observer<in T>) {
        observers.add(observer)
    }

    protected fun onNext(context: T) {
        observers.forEach { it.onNext(context) }
    }

    override fun close() {
        observers.forEach { it.onComplete() }
    }
}
