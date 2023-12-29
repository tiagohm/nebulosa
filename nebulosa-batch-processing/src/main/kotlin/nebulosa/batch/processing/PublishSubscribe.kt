package nebulosa.batch.processing

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.Subject
import java.io.Closeable

interface PublishSubscribe<T : Any> : ObservableSource<T>, Observer<T>, Closeable {

    val subject: Subject<T>

    fun Observable<T>.transform() = this

    fun subscribe(onNext: Consumer<in T>): Disposable {
        return subject.transform().subscribe(onNext)
    }

    override fun subscribe(observer: Observer<in T>) {
        return subject.transform().subscribe(observer)
    }

    override fun onSubscribe(disposable: Disposable) {
        subject.onSubscribe(disposable)
    }

    override fun onNext(event: T) {
        subject.onNext(event)
    }

    override fun onError(e: Throwable) {
        subject.onError(e)
    }

    override fun onComplete() {
        subject.onComplete()
    }

    override fun close() {
        onComplete()
    }
}
