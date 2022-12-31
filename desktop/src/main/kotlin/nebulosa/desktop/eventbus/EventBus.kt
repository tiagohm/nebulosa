package nebulosa.desktop.eventbus

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

@Suppress("NOTHING_TO_INLINE")
class EventBus {

    @PublishedApi internal val subject = PublishSubject.create<Any>()

    inline fun post(event: Any) = subject.onNext(event)

    inline fun subscribeOn(scheduler: Scheduler) = subject.subscribeOn(scheduler)

    inline fun observeOn(scheduler: Scheduler) = subject.observeOn(scheduler)

    inline fun <R : Any> map(action: Function<Any, out R>) = subject.map(action)

    inline fun filter(action: Predicate<in Any>) = subject.filter(action)

    inline fun debounce(timeout: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) = subject.debounce(timeout, unit)

    inline fun subscribe(next: Consumer<Any>) = subject.subscribe(next)
}
