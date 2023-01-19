package nebulosa.desktop.core

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.desktop.logic.concurrency.JavaFXThreadExecutor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
class EventBus {

    @PublishedApi internal val subject = PublishSubject.create<Any>()

    init {
        if (LOG.isDebugEnabled) {
            subject.subscribe { LOG.debug("event posted: {}", it) }
        }
    }

    inline fun post(event: Any) = subject.onNext(event)

    inline fun subscribeOn(scheduler: Scheduler) = subject.subscribeOn(scheduler)

    inline fun observeOn(scheduler: Scheduler) = subject.observeOn(scheduler)

    inline fun <R : Any> map(action: Function<Any, out R>) = subject.map(action)

    inline fun filter(action: Predicate<in Any>) = subject.filter(action)

    @JvmName("filterAndCast")
    inline fun <R : Any> filter(action: Predicate<in Any>) = subject.filter(action).map { it as R }

    inline fun <reified R : Any> filterIsInstance() = filter<R> { it is R }

    inline fun <reified R : Any> filterIsInstance(action: Predicate<in R>) = filter<R> { it is R && action.test(it) }

    inline fun debounce(timeout: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) = subject.debounce(timeout, unit)

    inline fun subscribe(next: Consumer<in Any>) = subject.subscribe(next)

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(EventBus::class.java)

        inline fun <R : Any> Observable<R>.observeOnFXThread() = observeOn(Schedulers.from(JavaFXThreadExecutor))
    }
}
