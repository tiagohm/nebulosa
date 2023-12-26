package nebulosa.api.focusers

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.io.Closeable
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class FocuserEventHandler(
    private val messageService: MessageService,
) : Closeable {

    private val throttler = PublishSubject.create<FocuserEvent>()

    init {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFocuserEvent(event: FocuserEvent) {
        when (event) {
            is PropertyChangedEvent -> throttler.onNext(event)
            is FocuserAttached -> sendMessage(FOCUSER_ATTACHED, event.device)
            is FocuserDetached -> sendMessage(FOCUSER_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: Focuser) {
        messageService.sendMessage(FocuserMessageEvent(eventName, device))
    }

    fun sendUpdate(device: Focuser) {
        sendMessage(FOCUSER_UPDATED, device)
    }

    override fun close() {
        throttler.onComplete()
    }

    companion object {

        const val FOCUSER_UPDATED = "FOCUSER_UPDATED"
        const val FOCUSER_ATTACHED = "FOCUSER_ATTACHED"
        const val FOCUSER_DETACHED = "FOCUSER_DETACHED"
    }
}
