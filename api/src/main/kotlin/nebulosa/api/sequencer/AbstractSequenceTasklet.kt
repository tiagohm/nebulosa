package nebulosa.api.sequencer

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.Closeable

abstract class AbstractSequenceTasklet<T : Any> : SequenceTasklet<T>, Closeable {

    private val publisher = PublishSubject.create<T>()

    override fun subscribe(onNext: Consumer<in T>): Disposable {
        return publisher.subscribe(onNext)
    }

    override fun subscribe(observer: Observer<in T>) {
        return publisher.subscribe(observer)
    }

    final override fun onSubscribe(disposable: Disposable) {
        publisher.onSubscribe(disposable)
    }

    final override fun onNext(event: T) {
        publisher.onNext(event)
    }

    final override fun onError(e: Throwable) {
        publisher.onError(e)
    }

    final override fun onComplete() {
        publisher.onComplete()
    }

    final override fun close() {
        onComplete()
    }
}
