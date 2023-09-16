package nebulosa.api.focusers

import io.reactivex.rxjava3.subjects.PublishSubject
import jakarta.annotation.PostConstruct
import nebulosa.api.beans.Subscriber
import nebulosa.api.services.MessageService
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Subscriber
class FocuserEventHandler(
    private val messageService: MessageService,
) {

    private val throttler = PublishSubject.create<FocuserEvent>()

    @PostConstruct
    private fun initialize() {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe { sendUpdate(it.device!!) }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFocuserEvent(event: FocuserEvent) {
        when (event) {
            is PropertyChangedEvent -> {
                throttler.onNext(event)
            }
            is FocuserAttached -> {
                messageService.sendMessage(FOCUSER_ATTACHED, event.device)
            }
            is FocuserDetached -> {
                messageService.sendMessage(FOCUSER_DETACHED, event.device)
            }
        }
    }

    fun sendUpdate(device: Focuser) {
        messageService.sendMessage(FOCUSER_UPDATED, device)
    }

    companion object {

        const val FOCUSER_UPDATED = "FOCUSER_UPDATED"
        const val FOCUSER_ATTACHED = "FOCUSER_ATTACHED"
        const val FOCUSER_DETACHED = "FOCUSER_DETACHED"
    }
}
