package nebulosa.api.sequencer

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.batch.processing.SimpleJob
import java.io.Closeable

abstract class ObservableJob<T : Any> : SimpleJob(), ObservableSource<T>, Observer<T>, Closeable {

    @JvmField protected val subject = PublishSubject.create<T>()

    protected open fun Observable<T>.transform() = this

    fun subscribe(onNext: Consumer<in T>): Disposable {
        return subject.transform().subscribe(onNext)
    }

    final override fun subscribe(observer: Observer<in T>) {
        return subject.transform().subscribe(observer)
    }

    final override fun onSubscribe(disposable: Disposable) {
        subject.onSubscribe(disposable)
    }

    final override fun onNext(event: T) {
        subject.onNext(event)
    }

    final override fun onError(e: Throwable) {
        subject.onError(e)
    }

    final override fun onComplete() {
        subject.onComplete()
    }

    final override fun close() {
        onComplete()
    }
}
