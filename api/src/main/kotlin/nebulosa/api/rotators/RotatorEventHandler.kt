package nebulosa.api.rotators

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.messages.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.rotator.RotatorAttached
import nebulosa.indi.device.rotator.RotatorDetached
import nebulosa.indi.device.rotator.RotatorEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.io.Closeable
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class RotatorEventHandler(
    private val messageService: MessageService,
) : Closeable {

    private val throttler = PublishSubject.create<RotatorEvent>()

    init {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onRotatorEvent(event: RotatorEvent) {
        when (event) {
            is PropertyChangedEvent -> throttler.onNext(event)
            is RotatorAttached -> sendMessage(ROTATOR_ATTACHED, event.device)
            is RotatorDetached -> sendMessage(ROTATOR_DETACHED, event.device)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(eventName: String, device: Rotator) {
        messageService.sendMessage(RotatorMessageEvent(eventName, device))
    }

    fun sendUpdate(device: Rotator) {
        sendMessage(ROTATOR_UPDATED, device)
    }

    override fun close() {
        throttler.onComplete()
    }

    companion object {

        const val ROTATOR_UPDATED = "ROTATOR.UPDATED"
        const val ROTATOR_ATTACHED = "ROTATOR.ATTACHED"
        const val ROTATOR_DETACHED = "ROTATOR.DETACHED"
    }
}
