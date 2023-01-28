package nebulosa.desktop.logic

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import javafx.application.Platform
import nebulosa.desktop.logic.connection.ConnectionEvent
import nebulosa.desktop.logic.task.TaskEvent
import nebulosa.indi.device.DeviceEvent
import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class EventBus<T : Any> {

    private val bus = PublishSubject.create<T>()

    fun subscribe(
        filter: Predicate<in T>? = null,
        observeOnJavaFX: Boolean = false,
        debounce: Duration? = null,
        next: (T) -> Unit,
    ): Disposable {
        return bus
            .let { if (filter != null) it.filter(filter) else it }
            .let { if (debounce != null) it.debounce(debounce.toSeconds(), TimeUnit.SECONDS) else it }
            .let { if (observeOnJavaFX) it.observeOn(Schedulers.from(JAVAFX_THREAD_EXECUTOR)) else it }
            .subscribe(next)
    }

    fun post(event: T) = bus.onNext(event)

    companion object {

        @JvmStatic private val JAVAFX_THREAD_EXECUTOR = Executor { Platform.runLater(it) }

        @JvmStatic val CONNECTION = EventBus<ConnectionEvent>()
        @JvmStatic val DEVICE = EventBus<DeviceEvent<*>>()
        @JvmStatic val TASK = EventBus<TaskEvent>()
    }
}
