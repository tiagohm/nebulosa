package nebulosa.api.wheels

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.filterwheel.FilterWheelEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.io.Closeable
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class WheelEventHandler(
    private val messageService: MessageService,
) : Closeable {

    private val throttler = PublishSubject.create<FilterWheelEvent>()

    init {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFocuserEvent(event: FilterWheelEvent) {
        when (event) {
            is PropertyChangedEvent -> throttler.onNext(event)
            is FilterWheelAttached -> sendMessage(WHEEL_ATTACHED, event.device)
            is FilterWheelDetached -> sendMessage(WHEEL_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: FilterWheel) {
        messageService.sendMessage(WheelMessageEvent(eventName, device))
    }

    fun sendUpdate(device: FilterWheel) {
        sendMessage(WHEEL_UPDATED, device)
    }

    override fun close() {
        throttler.onComplete()
    }

    companion object {

        const val WHEEL_UPDATED = "WHEEL_UPDATED"
        const val WHEEL_ATTACHED = "WHEEL_ATTACHED"
        const val WHEEL_DETACHED = "WHEEL_DETACHED"
    }
}
