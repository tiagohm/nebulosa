package nebulosa.api.sequencer

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.io.Closeable

abstract class PublishSequenceTasklet<T : SequenceTaskletEvent>(@JvmField protected val subject: Subject<T>) : SequenceTasklet<T>, Closeable {

    constructor() : this(PublishSubject.create<T>())

    protected open fun Observable<T>.transform() = this

    final override fun subscribe(onNext: Consumer<in T>): Disposable {
        return subject.transform().subscribe(onNext)
    }

    final override fun subscribe(observer: Observer<in T>) {
        return subject.transform().subscribe(observer)
    }

    final override fun onSubscribe(disposable: Disposable) {
        subject.onSubscribe(disposable)
    }

    @Synchronized
    final override fun onNext(event: T) {
        LOG.debug { "$event" }
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

    companion object {

        @JvmStatic private val LOG = loggerFor<PublishSequenceTasklet<*>>()
    }
}
