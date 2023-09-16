package nebulosa.api.sequencer

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.io.Closeable

abstract class SubjectSequenceTasklet<T : Any>(@JvmField protected val subject: Subject<T>) : SequenceTasklet<T>, Closeable {

    constructor() : this(PublishSubject.create<T>())

    override fun subscribe(onNext: Consumer<in T>): Disposable {
        return subject.subscribe(onNext)
    }

    override fun subscribe(observer: Observer<in T>) {
        return subject.subscribe(observer)
    }

    final override fun onSubscribe(disposable: Disposable) {
        subject.onSubscribe(disposable)
    }

    @Synchronized
    final override fun onNext(event: T) {
        subject.onNext(event)
    }

    @Synchronized
    final override fun onError(e: Throwable) {
        subject.onError(e)
    }

    @Synchronized
    final override fun onComplete() {
        subject.onComplete()
    }

    final override fun close() {
        onComplete()
    }
}
