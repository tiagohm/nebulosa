@file:Suppress("NOTHING_TO_INLINE")

package nebulosa.desktop.logic

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import javafx.application.Platform
import nebulosa.desktop.logic.connection.ConnectionEvent
import nebulosa.desktop.logic.task.TaskEvent
import nebulosa.indi.device.DeviceEvent
import java.util.concurrent.Executor

typealias EventBus<T> = PublishSubject<T>

typealias ConnectionEventBus = EventBus<ConnectionEvent>
typealias DeviceEventBus = EventBus<DeviceEvent<*>>
typealias TaskEventBus = EventBus<TaskEvent>

@PublishedApi internal val JAVAFX_THREAD_EXECUTOR = Executor { Platform.runLater(it) }
@PublishedApi internal val JAVAFX_SCHEDULER = Schedulers.from(JAVAFX_THREAD_EXECUTOR)

inline fun <T : Any> Observable<T>.observeOnJavaFX() = observeOn(JAVAFX_SCHEDULER)

inline fun <T> newEventBus(): EventBus<T> = PublishSubject.create()
