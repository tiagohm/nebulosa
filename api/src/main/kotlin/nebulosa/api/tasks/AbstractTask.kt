package nebulosa.api.tasks

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer

abstract class AbstractTask<T : Any> : Observable<T>(), ObservableTask<T> {

    private val observers = LinkedHashSet<Observer<in T>>(1)

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
