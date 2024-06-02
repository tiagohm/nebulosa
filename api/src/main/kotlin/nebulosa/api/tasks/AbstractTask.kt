package nebulosa.api.tasks

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import java.util.concurrent.atomic.AtomicReference

abstract class AbstractTask<T : Any> : Observable<T>(), ObservableTask<T> {

    private val observers = LinkedHashSet<Observer<in T>>(1)
    private val lastEvent = AtomicReference<T>()

    protected open fun canUseAsLastEvent(event: T) = true

    final override fun get(): T? = lastEvent.get()

    final override fun subscribeActual(observer: Observer<in T>) {
        observers.add(observer)
    }

    protected fun onNext(event: T) {
        if (canUseAsLastEvent(event)) {
            lastEvent.set(event)
        }

        observers.forEach { it.onNext(event) }
    }

    override fun close() {
        observers.forEach { it.onComplete() }
    }
}
